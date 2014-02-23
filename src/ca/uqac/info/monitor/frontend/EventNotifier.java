/*
	BeepBeep, an LTL-FO+ runtime monitor with XML events
	Copyright (C) 2008-2013 Sylvain Hallé

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.info.monitor.frontend;

import ca.uqac.info.monitor.*;
import ca.uqac.info.monitor.Event.EventException;
import ca.uqac.info.monitor.classify.*;
import ca.uqac.info.util.PipeCallback;

import java.io.*;
import java.util.Formatter;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

/**
 * 
 * <strong>NOTE:</strong> We must make sure that all methods that iterate
 * over the vector of monitors are <strong>synchronized</strong>, to avoid
 * one thread accessing this vector and modifying its contents while
 * another thread iterates over it.
 * @author sylvain
 *
 */
public class EventNotifier implements PipeCallback<String>
{
  protected Vector<MonitorInfo> m_minfo;
  protected int m_numEvents = 0;
  public boolean m_notifyOnEvents = false;
  public boolean m_notifyOnVerdict = true;
  public boolean m_mirrorEventsOnStdout = false;
  public boolean m_csvToStdout = false;
  public long m_totalTime = 0;
  public long heapSize = 0;
  public int m_slowdown = 0;
  protected final Formatter m_format;
  

  public EventNotifier()
  {
    m_minfo = new Vector<MonitorInfo>();
    m_format = new Formatter();
  }
  
  public EventNotifier(boolean notifyOnEvents)
  {
    this();
    m_notifyOnEvents = notifyOnEvents;
  }

  public synchronized void addMonitor(Monitor w)
  {
    addMonitor(w, new HashMap<String,String>());
  }
  
  public synchronized void addMonitor(Monitor w, Map<String,String> metadata)
  {
    MonitorInfo mi = new MonitorInfo();
    mi.m_monitor = w;
    mi.m_verdict = Monitor.Verdict.INCONCLUSIVE;
    mi.m_metadata = metadata;
    m_minfo.add(mi);
  }
  
  /**
   * Returns info about a monitor, fetched according to the value of
   * some metadata field.
   * @param p_name The metadata parameter to look for
   * @param p_value The value of the parameter the sought monitor
   *   must have
   * @return The monitor's info, null if not found
   */
  public synchronized int getMonitorIndex(String p_name, String p_value)
  {
    for (int i = 0; i < m_minfo.size(); i++)
    {
      MonitorInfo mi = m_minfo.elementAt(i);
      Map<String,String> metadata = mi.m_metadata;
      if (metadata == null)
        return -1;
      String value = metadata.get(p_name);
      if (value == null)
        return -1;
      if (value.compareTo(p_value) == 0)
      {
        return i;
      }
    }
    return -1;
  }
  
  public synchronized MonitorInfo getMonitorInfo(String p_name, String p_value)
  {
    int index = getMonitorIndex(p_name, p_value);
    if (index < 0)
      return null;
    return m_minfo.elementAt(index);
  }
  
  public synchronized MonitorInfo getMonitorInfo(int index)
  {
    if (index < 0 || index >= m_minfo.size())
      return null;
    return m_minfo.elementAt(index);
  }

  public int eventCount()
  {
    return m_numEvents;
  }

  @Override
  public synchronized void notify(String token, long buffer_size) throws CallbackException
  {
    //System.out.println(ESC_HOME + ESC_CLEARLINE + "Event received");
    if (m_mirrorEventsOnStdout)
    {
      System.out.print(token);
    }
    // Update all monitors
    Event e = null;
    try
    {
      e = new XPathEvent(token);
    }
    catch (EventException e1)
    {
      // Some problem occurred when making an event out of the received
      // token; print a warning and ignore the event
      e1.printStackTrace();
      return;
    }
    m_numEvents++; // Increment counter only if valid event
    long processing_time = 0;
    for (int i = 0; i < m_minfo.size(); i++)
    {
      long clock_start = System.nanoTime();
      MonitorInfo mi = m_minfo.elementAt(i);
      Monitor m = mi.m_monitor;
      mi.m_numEvents++;
      Monitor.Verdict old_out = mi.m_verdict;
      try
      {
        m.processEvent(e);
      }
      catch (MonitorException ex)
      {
        throw new CallbackException(ex.getMessage());
      }
      Monitor.Verdict new_out = m.getVerdict();
      if (m_slowdown > 0)
      {
        try
        {
          // We force the monitor to slow down by sleeping N ms
          Thread.sleep(m_slowdown);
        }
        catch (InterruptedException ie)
        {
          // TODO Auto-generated catch block
          ie.printStackTrace();
        }
      }
      long clock_end = System.nanoTime();
      processing_time = clock_end - clock_start;
      m_totalTime += processing_time;
      mi.m_verdict = m.getVerdict();
      mi.m_lastTime = processing_time;
      mi.m_cumulativeTime += processing_time;
      if (old_out != new_out && m_notifyOnVerdict)
      {
        Map<String,String> metadata = mi.m_metadata;
        String command = null;
        if (new_out == Monitor.Verdict.TRUE)
          command = metadata.get("OnTrue");
        if (new_out == Monitor.Verdict.FALSE)
          command = metadata.get("OnFalse");
        if (command != null)
        {
          try
          {
            File f = new File(metadata.get("Filename"));
            String absolute_path = f.getAbsolutePath();
            String s_dir = absolute_path.substring(0, absolute_path.lastIndexOf(File.separator));
            File dir = new File(s_dir);
            Runtime.getRuntime().exec("./" + command, null, dir);
          }
          catch (IOException ioe)
          {
            // TODO Auto-generated catch block
            ioe.printStackTrace();
          }
        }
      }
    }
    heapSize = Math.max(heapSize, Runtime.getRuntime().totalMemory());
    if (m_notifyOnEvents)
    {
      Formatter format = new Formatter();
      format.format("\r%4d |%3d ms |%6d ms |%5d MB |%5d MB |%s          ", m_numEvents, (int) (processing_time / 1000000f), (int) (m_totalTime / 1000000f), (int) (heapSize / 1048576f), (int) (buffer_size / 1048576f), formatVerdicts());
      System.err.print(format.toString());
      format.close();
    }
    if (m_csvToStdout)
    {
      m_format.format("%d,%d,%d,%d,%d,%s", m_numEvents, (int) (processing_time / 1000000f), (int) (m_totalTime / 1000000f), (int) (heapSize / 1048576f), buffer_size, m_minfo);
      System.out.println(m_format.toString());
    }
  }

  public synchronized void reset()
  {
    m_numEvents = 0;
    m_totalTime = 0;
    for (int i = 0; i < m_minfo.size(); i++)
    {
      MonitorInfo mi = m_minfo.elementAt(i);
      mi.reset();
    }
    if (m_notifyOnEvents)
    {
      printHeader();
    }
  }

  public synchronized void reset(int i)
  {
    m_numEvents = 0;
    MonitorInfo mi = m_minfo.elementAt(i);
    mi.reset();
  }
  
  protected synchronized void printHeader()
  {
    System.err.print("\nMsgs |Last   |Total     |Max heap |Buffer   |");
    for (int i = 0; i < m_minfo.size(); i++)
    {
      MonitorInfo mi = m_minfo.elementAt(i);
      Map<String,String> metadata = mi.m_metadata;
      String caption = metadata.get("Caption");
      if (caption == null || caption.equals(""))
        System.err.print("· ");
      else
        System.err.print(caption + " ");  
    }
    System.err.println(" ");
  }
  
  public synchronized String formatVerdicts()
  {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < m_minfo.size(); i++)
    {
      MonitorInfo mi = m_minfo.elementAt(i);
      Map<String,String> metadata = mi.m_metadata;
      Monitor.Verdict v = mi.m_verdict;
      String caption = metadata.get("Caption");
      if (caption == null)
        caption = "";
      if (v == Monitor.Verdict.INCONCLUSIVE)
        out.append("?");
      else if (v == Monitor.Verdict.TRUE)
        out.append("⊤");
      else if (v == Monitor.Verdict.FALSE)
        out.append("⊥");
      // We just pad the output with spaces to align with the length of the monitor's caption
      int length = caption.length();
      if (length == 0)
        length = 1;
      String space_pad = new String(new char[length]).replace("\0", " ");
      out.append(space_pad);
    }
    return out.toString();
  }
  
  public synchronized String getHolograms(String hologram_options)
  {
    StringBuilder out = new StringBuilder();
    for (MonitorInfo mi : m_minfo)
    {
      Monitor m = mi.m_monitor;
      if (hologram_options.contains("q"))
      {
        MonitorMarker mm = new QuantifiedValuesMarker();
        mm.mark(m);
      }
      if (hologram_options.contains("p"))
      {
        MonitorMarker mm = new PolarityMarker(false);
        mm.mark(m);
      }
      if (hologram_options.contains("P"))
      {
        MonitorMarker mm = new PolarityMarker(true);
        mm.mark(m);
      }
      if (hologram_options.contains("f"))
      {
        MonitorMarker mm = new FailFastMarker();
        mm.mark(m);        
      }
      DotVisitor dv = null;
      if (hologram_options.contains("L"))
      {
        // Output hologram for processing with LaTeX
        dv = new DotLatexVisitor();
      }
      else
      {
        // Output hologram with HTML symbols
        dv = new DotHtmlVisitor();
      }
      if (hologram_options.contains("c"))
      {
        // Output hologram without colors
        dv.setColor(false);
      }
      m.instanceAcceptPostfix(dv);
      String graph_title = "";
      MonitorDigestor md = new MonitorDigestor();
      m.instanceAcceptPostfix(md);
      if (hologram_options.contains("d"))
      {
        graph_title = md.getDigest();
      }
      if (hologram_options.contains("D"))
      {
        out.append(graph_title);
      }
      else
      {
        out.append(dv.toDot(graph_title));
      }
    }
    return out.toString();
  }
  
  public static class MonitorInfo
  {
    public Monitor.Verdict m_verdict;
    public Monitor m_monitor;
    public Map<String,String> m_metadata;
    public int m_numEvents;
    public long m_lastTime;
    public long m_cumulativeTime;
    
    public void reset()
    {
      m_verdict = Monitor.Verdict.INCONCLUSIVE;
      m_monitor.reset();
      m_numEvents = 0;
      m_lastTime = 0;
      m_cumulativeTime = 0;
    }
  }
}
