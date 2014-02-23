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
package ca.uqac.info.monitor.classify;

import ca.uqac.info.ltl.Operator;
import ca.uqac.info.ltl.Operator.ParseException;
import ca.uqac.info.monitor.Monitor;
import ca.uqac.info.monitor.MonitorException;
import ca.uqac.info.monitor.MonitorFactory;
import ca.uqac.info.monitor.XPathEvent;
import ca.uqac.info.monitor.Event.EventException;

public class DotLatexVisitor extends DotVisitor
{
  protected void setSymbols()
  {
    m_forAllSymbol = "\\forall";
    m_existsSymbol = "\\exists";
    m_inSymbol = "\\in";
    m_andSymbol = "\\wedge";
    m_orSymbol = "\\vee";
    m_xorSymbol = "\\oplus";
    m_impliesSymbol = "\\rightarrow";
    m_notSymbol = "\\neg";
    m_GSymbol = "\\mbox{\\bf G}";
    m_USymbol = "\\mbox{\\bf U}";
    m_XSymbol = "\\mbox{\\bf X}";
    m_FSymbol = "\\mbox{\\bf F}";
    m_trueSymbol = "\\top";
    m_falseSymbol = "\\bot";
    m_greaterThanSymbol = ">";
  }
  
  protected String formatNode(String symbol, Monitor m)
  {
    String fill_color = getColor(m);
    String line_style = getLineStyle(m);
    return " [texlbl=\"$" + symbol + formatVerdict(m) + "$\",shape=rect,style=\"filled," + line_style + "\",height=0.25,fillcolor=\"" + fill_color + "\"];\n";
  }
  
  protected String formatDummyNode(String join_symbol, Monitor m)
  {
    StringBuilder out = new StringBuilder();
    String line_color = getLineColor(m);
    String line_style = getLineStyle(m);
    out.append(" [shape=filled,fillcolor=\"").append(getColor(m)).append("\",texlbl=\"$").append(join_symbol).append("$\",shape=circle,height=0.2,fixedsize=true,color=\"").append(line_color).append("\",style=\"").append(line_style).append("\"];\n");
    return out.toString();
  }
  
  protected String formatVerdict(Monitor m)
  {
    Monitor.Verdict v = m.getVerdict();
    if (v == Monitor.Verdict.TRUE)
      return "^{\\top}";
    if (v == Monitor.Verdict.FALSE)
      return "^{\\bot}";
    return "^{?}";
  }
  
  public static void main(String[] args)
  {
    Operator o = null;
    try
    {
      o = Operator.parseFromString("F (/p > 3)");
    } catch (ParseException e)
    {
      e.printStackTrace();
    }
    MonitorFactory mf = new MonitorFactory();
    o.accept(mf);
    Monitor m = mf.getMonitor();
    try
    {
      m.processEvent(new XPathEvent("<message><p>0</p></message>"));
      m.getVerdict();
      m.processEvent(new XPathEvent("<message><p>1</p></message>"));
      m.getVerdict();
      DotVisitor dv = new DotLatexVisitor();
      m.instanceAcceptPostfix(dv);
      System.out.println(dv.toDot());
    } catch (MonitorException e)
    {
      // Auto-generated catch block
      e.printStackTrace();
    } catch (EventException e)
    {
      // Auto-generated catch block
      e.printStackTrace();
    }
  }
}
