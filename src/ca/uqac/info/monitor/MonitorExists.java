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

import ca.uqac.info.ltl.Atom;
import ca.uqac.info.simplexpath.SimpleXPathExpression;

public class MonitorExists extends MonitorForAll
{
  public MonitorExists(Atom x, SimpleXPathExpression p, Monitor phi)
  {
    super(x, p, phi);
  }
  
  protected MonitorExists(Atom x, SimpleXPathExpression p, Monitor phi, Map<String,String> res)
  {
    super(x, p, phi, res);
  }
  
  @Override
  protected Verdict updateVerdict()
  {
    // If we already reached a verdict, return it
    if (m_verdict != Verdict.INCONCLUSIVE)
    {
      return m_verdict;
    }
    Iterator<ValuePair> it = m_mons.iterator();
    while (it.hasNext())
    {
      ValuePair vp = it.next();
      Monitor mon = vp.m_monitor;
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
    // If no internal monitor exists in the list, verdict is false
    if (m_mons.isEmpty() && this.m_verdict != Verdict.TRUE)
    {
      this.m_verdict = Verdict.FALSE;
    }
    // If we reached a verdict, clear all monitors for they are no longer
    // necessary
    if (CLEANUP && m_verdict != Verdict.INCONCLUSIVE)
      m_mons.clear();
    return this.m_verdict;
  }
  
  @Override
  public Monitor deepClone()
  {
    // Clones share the same XPath expression and resolver
    MonitorExists out = new MonitorExists(new Atom(m_x.toString()), 
        m_p, m_phi.deepClone(), m_variableResolver);
    return out;
  }
  
  @Override
  public String toString()
  {
    return "∃ " + m_x + " ∈ " + m_p + " : (" + m_phi + ")";
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
    for (ValuePair vp : m_mons)
    {
      Monitor m = vp.m_monitor;
      m.instanceAcceptPostfix(v);
    }
    v.visit(this);
  }
}
