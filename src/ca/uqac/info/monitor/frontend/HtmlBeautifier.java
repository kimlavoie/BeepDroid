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

import java.util.Stack;

import ca.uqac.info.ltl.*;
import ca.uqac.info.monitor.*;

/**
 * Provides an HTML rendition of the LTL-FO+ formula watched by a monitor.
 * @author sylvain
 *
 */
public class HtmlBeautifier implements MonitorVisitor
{
  
  protected Stack<StringBuilder> m_stack;
  
  public HtmlBeautifier()
  {
    super();
    m_stack = new Stack<StringBuilder>();
  }
  
  public String getFormula()
  {
    StringBuilder out = m_stack.peek();
    return out.toString();
  }

  @Override
  public void visit(final MonitorAnd m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder right = m_stack.pop();
    StringBuilder left = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-paren\">(</span>").append(left).append("<span class=\"ltlfo-paren\">)</span> <span class=\"ltlfo-and\">&and;</span> <span class=\"ltlfo-paren\">(</span>").append(right).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorEquals m)
  {
    StringBuilder out = new StringBuilder();
    out.append("<span class=\"ltlfo-block\">");
    out.append(wrap(m.getLeft())).append(" = ").append(wrap(m.getRight()));
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorExists m)
  {
    StringBuilder out = new StringBuilder();
    String variable = m.getVariable().toString();
    String path = m.getPath().toString();
    StringBuilder operand = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-exists\">&exists;</span> <span class=\"ltlfo-var\">").append(variable).append("</span> &in; <span class=\"ltlfo-path\">").append(path).append("</span> : (").append(operand).append(")");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorF m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder op = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-f\">F</span> <span class=\"ltlfo-paren\">(</span>").append(op).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorFalse m)
  {
    StringBuilder out = new StringBuilder();
    m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("&#x22A4;");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorForAll m)
  {
    StringBuilder out = new StringBuilder();
    String variable = m.getVariable().toString();
    String path = m.getPath().toString();
    StringBuilder operand = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-forall\">&forall;</span> <span class=\"ltlfo-var\">").append(variable).append("</span> &in; <span class=\"ltlfo-path\">").append(path).append("</span> : <span class=\"ltlfo-paren\">(</span>").append(operand).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorG m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder op = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-g\">G</span> <span class=\"ltlfo-paren\">(</span>").append(op).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorGreaterThan m)
  {
    StringBuilder out = new StringBuilder();
    out.append("<span class=\"ltlfo-block\">");
    out.append(wrap(m.getLeft())).append(" = ").append(wrap(m.getRight()));
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorImplies m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder right = m_stack.pop();
    StringBuilder left = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-paren\">(</span>").append(left).append("<span class=\"ltlfo-paren\">)</span> <span class=\"ltlfo-rarr\">&rarr;</span> <span class=\"ltlfo-paren\">(</span>").append(right).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorOr m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder right = m_stack.pop();
    StringBuilder left = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-paren\">(</span>").append(left).append("<span class=\"ltlfo-paren\">)</span> <span class=\"ltlfo-or\">&or;</span> <span class=\"ltlfo-paren\">(</span>").append(right).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorNot m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder op = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-not\">&not;</span> <span class=\"ltlfo-paren\">(</span>").append(op).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorTrue m)
  {
    StringBuilder out = new StringBuilder();
    m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("&#x22A5;");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorU m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder right = m_stack.pop();
    StringBuilder left = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-paren\">(</span>").append(left).append("<span class=\"ltlfo-paren\">)</span> <span class=\"ltlfo-u\">U</span> <span class=\"ltlfo-paren\">(</span>").append(right).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorX m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder op = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-x\">X</span> <span class=\"ltlfo-paren\">(</span>").append(op).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }

  @Override
  public void visit(final MonitorXor m)
  {
    StringBuilder out = new StringBuilder();
    StringBuilder right = m_stack.pop();
    StringBuilder left = m_stack.pop();
    out.append("<span class=\"ltlfo-block\">");
    out.append("<span class=\"ltlfo-paren\">(</span>").append(left).append("<span class=\"ltlfo-paren\">)</span> &oplus; <span class=\"ltlfo-paren\">(</span>").append(right).append("<span class=\"ltlfo-paren\">)</span>");
    out.append("</span>");
    m_stack.push(out);
  }
  
  protected static StringBuilder wrap(final Operator o)
  {
    StringBuilder out = new StringBuilder();
    if (o instanceof XPathAtom)
    {
      out.append("<span class=\"ltlfo-path\">").append(o.toString()).append("</span>");
    }
    else if (o instanceof Constant)
    {
      out.append(o.toString());
    }
    else
    {
      // o is a variable
      out.append("<span class=\"ltlfo-var\">").append(o.toString()).append("</span>");
    }
    return out;
  }
}