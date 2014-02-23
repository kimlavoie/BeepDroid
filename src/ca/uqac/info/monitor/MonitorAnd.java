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

public class MonitorAnd extends BinaryMonitor
{
  
  public MonitorAnd(Monitor l, Monitor r)
  {
    super(l, r);
  }

  @Override
  public void processEvent(Event e) throws MonitorException
  {
    if (m_verdict == Verdict.INCONCLUSIVE)
    {
      // Don't process events if a verdict has been reached
      m_left.processEvent(e);
      m_right.processEvent(e);
      m_verdict = updateVerdict();
    }
  }

  protected Verdict updateVerdict()
  {
    if (m_verdict == Verdict.INCONCLUSIVE)
    {
      Verdict left = m_left.getVerdict();
      Verdict right = m_right.getVerdict();
      m_verdict = threeValuedAnd(left, right);
    }
    return m_verdict;
  }

  @Override
  public Monitor deepClone()
  {
    return new MonitorAnd(m_left.deepClone(), m_right.deepClone());
  }
  
  @Override
  public String toString()
  {
    return "(" + m_left + ") ∧ (" + m_right + ")";
  }
  
  public void accept(MonitorVisitor v)
  {
    m_left.accept(v);
    m_right.accept(v);
    v.visit(this);
  }
  
  public void instanceAcceptPostfix(MonitorVisitor v)
  {
    m_left.instanceAcceptPostfix(v);
    m_right.instanceAcceptPostfix(v);
    v.visit(this);
  }
}
