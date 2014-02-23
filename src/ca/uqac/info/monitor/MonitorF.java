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
package ca.uqac.info.monitor;

import java.util.*;

public class MonitorF extends MonitorG
{  
  public MonitorF(Monitor m)
  {
    super(m);
  }
  
  @Override
  public Monitor deepClone()
  {
    MonitorF out = new MonitorF(m_phi.deepClone());
    return out;
  }

  @Override
  protected Verdict updateVerdict()
  {
    // If we already reached a verdict, return it
    if (this.m_verdict != Verdict.INCONCLUSIVE)
    {
      return this.m_verdict;
    }
    Iterator<Monitor> it = m_mons.iterator();
    while (it.hasNext())
    {
      Monitor mon = it.next();
      Verdict verd = mon.getVerdict();
      if (verd != Verdict.INCONCLUSIVE)
      {
        // We remove this monitor from the array, as it
        // will never change the verdict from now on
        if (CLEANUP)
        {
          it.remove();  
        }
      }
      if (verd == Verdict.TRUE)
      {
        this.m_verdict = Verdict.TRUE;
        break;
      }
    }
    // If we reached a verdict, clear all monitors for they are no longer
    // necessary
    if (CLEANUP && m_verdict != Verdict.INCONCLUSIVE)
      m_mons.clear();
    return this.m_verdict;
  }
  
  @Override
  public String toString()
  {
    return "F (" + m_phi + ")";
  }
  
  @Override
  public void accept(MonitorVisitor v)
  {
    m_phi.accept(v);
    v.visit(this);
  }
  
  @Override
  public void instanceAcceptPostfix(MonitorVisitor v)
  {
    for (Monitor m : m_mons)
    {
      m.instanceAcceptPostfix(v);
    }
    v.visit(this);
  }
}