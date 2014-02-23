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

import java.util.List;

import ca.uqac.info.monitor.*;

/**
 * The fail-fast marker tags for deletion all nodes of the hologram
 * that are not necessary to determine the value of a formula
 * @author sylvain
 *
 */
public class PolarityMarker extends PrefixMarker
{
  /**
   * If set to true, the marker will preserve the relative positioning when
   * deleting monitors under temporal operators by flagging for deletion only
   * grand-children of the current monitor
   */
  protected boolean m_preserveOffset = false;
  
  public PolarityMarker()
  {
    super();
  }
  
  public PolarityMarker(boolean preserveOffset)
  {
    this();
    m_preserveOffset = preserveOffset;
  }
  
  @Override
  protected void mark(MonitorAnd m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    if (left.getVerdict() == Monitor.Verdict.FALSE)
    {
      mark(right, true, false);
      mark(left, to_delete, false);
    }
    else if (right.getVerdict() == Monitor.Verdict.FALSE)
    {
      mark(left, to_delete, false);
      mark(right, true, false);      
    }
    else
    {
      mark(left, to_delete, false);
      mark(right, true, false);
    }
  }
  
  protected void mark(MonitorOr m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    if (left.getVerdict() == Monitor.Verdict.TRUE)
    {
      mark(right, true, except_yourself);
      mark(left, to_delete, except_yourself);
    }
    else if (right.getVerdict() == Monitor.Verdict.TRUE)
    {
      mark(left, to_delete, false);
      mark(right, true, false);      
    }
    else
    {
      mark(left, to_delete, false);
      mark(right, true, false);
    }
  }
  
  protected void mark(MonitorImplies m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    if (left.getVerdict() == Monitor.Verdict.FALSE)
    {
      mark(right, true, false);
    }
    else if (right.getVerdict() == Monitor.Verdict.TRUE)
    {
      mark(left, true, false);
    }
    else
    {
      mark(left, to_delete, false);
      mark(right, to_delete, false);
    }
  }
  
  protected void mark(MonitorG m, boolean to_delete, boolean except_yourself)
  {
    Monitor.Verdict v = m.getVerdict();
    List<Monitor> mons = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    boolean seen_false = false;
    for (Monitor mon : mons)
    {
      Monitor.Verdict mon_verdict = mon.getVerdict();
      if (to_delete)
      {
        mark(mon, to_delete, false);
        continue;
      }
      if (mon_verdict == Monitor.Verdict.FALSE)
      {
        if (seen_false) // We have already seen a false monitor, no need to keep another one
          mark(mon, true, false);
        else
          mark(mon, to_delete, false);
        to_delete = true;
        seen_false = true;
      }
      else if (mon_verdict == Monitor.Verdict.TRUE)
      {
        if (v == Monitor.Verdict.FALSE)
          mark(mon, true, m_preserveOffset);
        else
          mark(mon, to_delete, m_preserveOffset);
      }
      else // mon_verdict == INCONCLUSIVE
      {
        if (v != Monitor.Verdict.INCONCLUSIVE)
          mark(mon, true, m_preserveOffset); // No need to keep inconclusive mons unless parent also is
        else
          mark(mon, to_delete, m_preserveOffset);
      }
    }
  }
  
  protected void mark(MonitorF m, boolean to_delete, boolean except_yourself)
  {
    Monitor.Verdict v = m.getVerdict();
    List<Monitor> mons = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    boolean seen_true = false;
    for (Monitor mon : mons)
    {
      Monitor.Verdict mon_verdict = mon.getVerdict();
      if (to_delete)
      {
        mark(mon, to_delete, false);
        continue;
      }
      if (mon_verdict == Monitor.Verdict.TRUE)
      {
        if (seen_true)
          mark(mon, true, false);
        else
          mark(mon, to_delete, false);
        to_delete = true;
        seen_true = true;
      }
      else if (mon_verdict == Monitor.Verdict.FALSE)
      {
        if (v == Monitor.Verdict.TRUE)
          mark(mon, true, m_preserveOffset);
        else
          mark(mon, to_delete, m_preserveOffset);
      }
      else // mon_verdict == INCONCLUSIVE
      {
        if (v != Monitor.Verdict.INCONCLUSIVE)
          mark(mon, true, m_preserveOffset); // No need to keep inconclusive mons unless parent also is
        else
          mark(mon, to_delete, m_preserveOffset);
      }
    }
  }
  
  protected void mark(MonitorForAll m, boolean to_delete, boolean except_yourself)
  {
    Monitor.Verdict v = m.getVerdict();
    List<MonitorQuantifier.ValuePair> vps = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    boolean seen_false = false;
    for (MonitorQuantifier.ValuePair vp : vps)
    {
      Monitor mon = vp.getMonitor();
      Monitor.Verdict mon_verdict = mon.getVerdict();
      if (to_delete)
      {
        mark(mon, to_delete, false);
        continue;
      }
      if (mon_verdict == Monitor.Verdict.FALSE)
      {
        if (seen_false) // We have already seen a false monitor, no need to keep another one
          mark(mon, true, false);
        else
          mark(mon, to_delete, false);
        to_delete = true;
        seen_false = true;
      }
      else if (mon_verdict == Monitor.Verdict.TRUE)
      {
        if (v == Monitor.Verdict.FALSE)
          mark(mon, true, false);
        else
          mark(mon, to_delete, false);
      }
      else // mon_verdict == INCONCLUSIVE
      {
        if (v != Monitor.Verdict.INCONCLUSIVE)
          mark(mon, true, false); // No need to keep inconclusive mons unless parent also is
        else
          mark(mon, to_delete, false);
      }
    }
  }
  
  protected void mark(MonitorExists m, boolean to_delete, boolean except_yourself)
  {
    Monitor.Verdict v = m.getVerdict();
    List<MonitorQuantifier.ValuePair> vps = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    boolean seen_true = true;
    for (MonitorQuantifier.ValuePair vp : vps)
    {
      Monitor mon = vp.getMonitor();
      Monitor.Verdict mon_verdict = mon.getVerdict();
      if (to_delete)
      {
        mark(mon, to_delete, false);
        continue;
      }
      if (mon_verdict == Monitor.Verdict.TRUE)
      {
        if (seen_true)
          mark(mon, true, false);
        else
          mark(mon, to_delete, false);
        to_delete = true;
        seen_true = true;
      }
      else if (mon_verdict == Monitor.Verdict.FALSE)
      {
        if (v == Monitor.Verdict.TRUE)
          mark(mon, true, false);
        else
          mark(mon, to_delete, false);
      }
      else // mon_verdict == INCONCLUSIVE
      {
        if (v != Monitor.Verdict.INCONCLUSIVE)
          mark(mon, true, false); // No need to keep inconclusive mons unless parent also is
        else
          mark(mon, to_delete, false);
      } 
    }
  }
}