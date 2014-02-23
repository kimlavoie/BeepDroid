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

import ca.uqac.info.ltl.*;
import ca.uqac.info.simplexpath.SimpleXPathExpression;

import java.util.*;

public class MonitorForAll extends MonitorQuantifier
{
  
  public MonitorForAll(Atom x, SimpleXPathExpression p, Monitor phi)
  {
    super(x, p, phi);
  }
  
  protected MonitorForAll(Atom x, SimpleXPathExpression p, Monitor phi, Map<String,String> res)
  {
    super(x, p, phi, res);
  }

  @Override
  public void processEvent(Event e) throws MonitorException
  {
    // Optimization: if we already reached a verdict,
    // don't care about new events
    if (m_verdict != Verdict.INCONCLUSIVE && SKIP_ON_VERDICT)
    {
      return;
    }
    // Fetch domain for quantified variable
    if (m_firstEvent == true)
    {
      Set<Constant> domain = e.evaluate(m_p, m_variableResolver);
      m_firstEvent = false;
      for (Constant value : domain)
      {
        // For each value, instantiate inner monitor, replacing the variable
        // by this value
        Monitor new_mon = m_phi.deepClone();
        new_mon.setValue(m_x, value);
        m_mons.add(new ValuePair(value, new_mon));
      }
    }
    // Have all the monitors process the current event
    if (MULTI_THREAD)
    {
      // ...using multiple threads
      /*List<MonitorThread> threads = new LinkedList<MonitorThread>();
      for (Monitor mon : m_mons)
      {
        MonitorThread mt = new MonitorThread(mon, e);
        threads.add(mt);
        mt.start();
      }
      // Loop until all threads are stopped
      boolean all_stopped = false;
      while (!all_stopped)
      {
        all_stopped = true;
        for (MonitorThread mt : threads)
        {
          if (mt.getState() != Thread.State.TERMINATED)
            all_stopped = false;
        }
      } */   
    }
    else
    {
      // ...using a single thread
      for (ValuePair vp : m_mons)
      {
        Monitor mon = vp.m_monitor;
        mon.processEvent(e);
      }
    }
    m_verdict = updateVerdict();
  }

  protected Verdict updateVerdict()
  {
    // If we already reached a verdict, return it
    if (m_verdict != Verdict.INCONCLUSIVE)
    {
      return m_verdict;
    }
    Iterator<ValuePair> it = m_mons.iterator();
    boolean seen_inconclusive = false;
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
      else
      {
        seen_inconclusive = true;
      }
      if (verd == Verdict.FALSE)
      {
        this.m_verdict = Verdict.FALSE;
        if (SKIP_ON_VERDICT)
          break;
      }
    }
    // If all monitors evaluate to true, verdict is true
    if (!seen_inconclusive && this.m_verdict != Verdict.FALSE)
    {
      this.m_verdict = Verdict.TRUE;
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
    MonitorForAll out = new MonitorForAll(new Atom(m_x.toString()), 
       m_p, m_phi.deepClone(), m_variableResolver);
    return out;
  }
  
  @Override
  public String toString()
  {
    return "∀ " + m_x + " ∈ " + m_p + " : (" + m_phi + ")";
  }

  @Override
  public void accept(MonitorVisitor v)
  {
    m_phi.accept(v);
    v.visit(this);
  }
  
  public SimpleXPathExpression getPath()
  {
    return m_p;
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
