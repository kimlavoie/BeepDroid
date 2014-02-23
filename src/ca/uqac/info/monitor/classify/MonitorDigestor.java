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

import java.util.Stack;

import ca.uqac.info.ltl.Atom;
import ca.uqac.info.monitor.*;

public class MonitorDigestor implements MonitorVisitor
{
  protected StringBuilder m_digest;
  
  protected Stack<StringBuilder> m_fragments;
  
  protected static final String PUSH_SYMBOL = "↓";
  protected static final String POP_SYMBOL = "↑";
  protected static final String SPACE = " ";
  
  public MonitorDigestor()
  {
    super();
    m_digest = new StringBuilder();
    m_fragments = new Stack<StringBuilder>();
  }
  
  public String getDigest()
  {
    StringBuilder out = m_fragments.peek();
    return out.toString();
  }
  
  protected static String formatVerdict(Monitor.Verdict v)
  {
    if (v == Monitor.Verdict.TRUE)
      return "⊤";
    else if (v == Monitor.Verdict.FALSE)
      return "⊥";
    return "⟀";
  }

  @Override
  public void visit(MonitorAnd m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("∧").append(SPACE).append(formatVerdict(v)).append(SPACE);
    StringBuilder right = m_fragments.pop();
    StringBuilder left = m_fragments.pop();
    out.append(left);
    if (left.length() > 0)
      out.append(SPACE);
    out.append(right);
    if (right.length() > 0)
      out.append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorEquals m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("=").append(SPACE).append(formatVerdict(v)).append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorExists m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("∃").append(SPACE).append(formatVerdict(v)).append(SPACE);
    for (MonitorQuantifier.ValuePair vp : m.getInstances())
    {
      StringBuilder sb = m_fragments.pop();
      if (sb.length() > 0)
      {
        Atom value = vp.getValue();
        out.append(value).append(SPACE).append(sb).append(SPACE);
      }
    }
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorF m)
  {
    StringBuilder out = new StringBuilder();
    if (m.isMarkedForDeletion())
    {
      m_fragments.push(out);
      return;
    }
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("F").append(SPACE).append(formatVerdict(v)).append(SPACE);
    for (int i = 0; i < m.getInstances().size(); i++)
    {
      StringBuilder sb = m_fragments.pop();
      out.append(sb);
      if (sb.length() > 0)
        out.append(SPACE);
    }
    out.append(POP_SYMBOL);
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorFalse m)
  {
    StringBuilder out = new StringBuilder();
    out.append(PUSH_SYMBOL).append(SPACE).append("⊥").append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorForAll m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("∀").append(SPACE).append(formatVerdict(v)).append(SPACE);
    for (MonitorQuantifier.ValuePair vp : m.getInstances())
    {
      StringBuilder sb = m_fragments.pop();
      if (sb.length() > 0)
      {
        Atom value = vp.getValue();
        out.append(value).append(SPACE).append(sb).append(SPACE);
      }
    }
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorG m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("G").append(SPACE).append(formatVerdict(v)).append(SPACE);
    for (int i = 0; i < m.getInstances().size(); i++)
    {
      StringBuilder sb = m_fragments.pop();
      out.append(sb);
      if (sb.length() > 0)
        out.append(SPACE);
    }
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorGreaterThan m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append(">").append(SPACE).append(formatVerdict(v)).append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorImplies m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("→").append(SPACE).append(formatVerdict(v)).append(SPACE);
    StringBuilder right = m_fragments.pop();
    StringBuilder left = m_fragments.pop();
    out.append(left);
    if (left.length() > 0)
      out.append(SPACE);
    out.append(right);
    if (right.length() > 0)
      out.append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorOr m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("∨").append(SPACE).append(formatVerdict(v)).append(SPACE);
    StringBuilder right = m_fragments.pop();
    StringBuilder left = m_fragments.pop();
    out.append(left);
    if (left.length() > 0)
      out.append(SPACE);
    out.append(right);
    if (right.length() > 0)
      out.append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorNot m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("¬").append(SPACE).append(formatVerdict(v)).append(SPACE);
    StringBuilder right = m_fragments.pop();
    out.append(right);
    if (right.length() > 0)
      out.append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorTrue m)
  {
    StringBuilder out = new StringBuilder();
    out.append(PUSH_SYMBOL).append(SPACE).append("⊤").append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorU m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("U").append(SPACE).append(formatVerdict(v)).append(SPACE);
    StringBuilder right = m_fragments.pop();
    StringBuilder left = m_fragments.pop();
    out.append(left);
    if (left.length() > 0)
      out.append(SPACE);
    out.append(right);
    if (right.length() > 0)
      out.append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorX m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("X").append(SPACE).append(formatVerdict(v)).append(SPACE);
    StringBuilder right = m_fragments.pop();
    out.append(right);
    if (right.length() > 0)
      out.append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }

  @Override
  public void visit(MonitorXor m)
  {
    StringBuilder out = new StringBuilder();
    Monitor.Verdict v = m.getVerdict();
    out.append(PUSH_SYMBOL).append(SPACE).append("⊕").append(SPACE).append(formatVerdict(v)).append(SPACE);
    StringBuilder right = m_fragments.pop();
    StringBuilder left = m_fragments.pop();
    out.append(left);
    if (left.length() > 0)
      out.append(SPACE);
    out.append(right);
    if (right.length() > 0)
      out.append(SPACE);
    out.append(POP_SYMBOL);
    if (m.isMarkedForDeletion())
    {
      out = new StringBuilder();
    }
    m_fragments.push(out);
  }
}
