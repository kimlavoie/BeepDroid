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
import java.util.Stack;

import ca.uqac.info.monitor.*;

/**
 * Simple marker that recurses through the monitor tree using a
 * prefix traversal. The marker merely passes on a Boolean value
 * through each node of the tree, but does not change this value.
 * This is intended as a utility class used to build other markers that will
 * override this default behaviour for some of the operators.
 * @author sylvain
 *
 */
public class PrefixMarker implements MonitorMarker
{
  /**
   * Collects in a stack the sequence of all operators visited from the
   * root of the monitor tree
   */
  protected Stack<String> m_operators;
  
  public PrefixMarker()
  {
    super();
    m_operators = new Stack<String>();
  }
  
  public void mark(Monitor m)
  {
    mark(m, false, false);
  }
  
  protected void mark(Monitor m, boolean to_delete, boolean except_yourself)
  {
    if (m instanceof MonitorAnd)
    {
      m_operators.push("and");
      mark((MonitorAnd) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorOr)
    {
      m_operators.push("or");
      mark((MonitorOr) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorXor)
    {
      m_operators.push("xor");
      mark((MonitorXor) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorNot)
    {
      m_operators.push("not");
      mark((MonitorNot) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorImplies)
    {
      m_operators.push("->");
      mark((MonitorImplies) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorG)
    {
      m_operators.push("G");
      mark((MonitorG) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorF)
    {
      m_operators.push("F");
      mark((MonitorF) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorX)
    {
      m_operators.push("X");
      mark((MonitorX) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorU)
    {
      m_operators.push("U");
      mark((MonitorU) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorForAll)
    {
      m_operators.push("forall");
      mark((MonitorForAll) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorExists)
    {
      m_operators.push("exists");
      mark((MonitorExists) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorEquals)
    {
      m_operators.push("=");
      mark((MonitorEquals) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorGreaterThan)
    {
      m_operators.push(">");
      mark((MonitorGreaterThan) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorTrue)
    {
      m_operators.push("true");
      mark((MonitorTrue) m, to_delete, except_yourself);
      m_operators.pop();
    }
    if (m instanceof MonitorFalse)
    {
      m_operators.push("false");
      mark((MonitorFalse) m, to_delete, except_yourself);
      m_operators.pop();
    }
  }
  
  protected void mark(MonitorAnd m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    mark(left, to_delete, false);
    mark(right, to_delete, false);
  }
  
  protected void mark(MonitorOr m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    mark(left, to_delete, false);
    mark(right, to_delete, false);
  }
  
  protected void mark(MonitorXor m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    mark(left, to_delete, false);
    mark(right, to_delete, false);
  }

  protected void mark(MonitorNot m, boolean to_delete, boolean except_yourself)
  {
    Monitor op = m.getOperand();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    mark(op, to_delete, false);
  }
  
  protected void mark(MonitorImplies m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    mark(left, to_delete, false);
    mark(right, to_delete, false);
  }
  
  protected void mark(MonitorX m, boolean to_delete, boolean except_yourself)
  {
    Monitor op = m.getOperand();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    mark(op, to_delete, false);
  }
  
  protected void mark(MonitorG m, boolean to_delete, boolean except_yourself)
  {
    List<Monitor> mons = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    for (Monitor mon : mons)
    {
      mark(mon, to_delete, false);
    }
  }
  
  protected void mark(MonitorF m, boolean to_delete, boolean except_yourself)
  {
    List<Monitor> mons = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    for (Monitor mon : mons)
    {
      mark(mon, to_delete, false);
    }
  }
  
  protected void mark(MonitorU m, boolean to_delete, boolean except_yourself)
  {
    Monitor left = m.getLeft();
    Monitor right = m.getRight();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    mark(left, to_delete, false);
    mark(right, to_delete, false);
  }
  
  protected void mark(MonitorEquals m, boolean to_delete, boolean except_yourself)
  {
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
  }
  
  protected void mark(MonitorGreaterThan m, boolean to_delete, boolean except_yourself)
  {
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
  }
  
  protected void mark(MonitorForAll m, boolean to_delete, boolean except_yourself)
  {
    List<MonitorQuantifier.ValuePair> vps = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    for (MonitorQuantifier.ValuePair vp : vps)
    {
      Monitor mon = vp.getMonitor();
      mark(mon, to_delete, false);
    }
  }
  
  protected void mark(MonitorExists m, boolean to_delete, boolean except_yourself)
  {
    List<MonitorQuantifier.ValuePair> vps = m.getInstances();
    if (to_delete && !except_yourself)
    {
      m.markForDeletion();
    }
    for (MonitorQuantifier.ValuePair vp : vps)
    {
      Monitor mon = vp.getMonitor();
      mark(mon, to_delete, false);
    }
  }
  
}