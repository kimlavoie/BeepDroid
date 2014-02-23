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

import java.util.*;

import ca.uqac.info.monitor.*;

public abstract class DotVisitor implements MonitorVisitor
{
  
  protected StringBuilder m_sb;
  protected Stack<Integer> m_nodeIds;
  protected Stack<Boolean> m_nodeMarked;
  protected int m_nodeCount;
  protected boolean m_inColor = true;
  
  // Constants for each symbol occurring in the tree
  protected static String m_forAllSymbol;
  protected static String m_existsSymbol;
  protected static String m_inSymbol;
  protected static String m_andSymbol;
  protected static String m_orSymbol;
  protected static String m_xorSymbol;
  protected static String m_impliesSymbol;
  protected static String m_notSymbol;
  protected static String m_GSymbol;
  protected static String m_USymbol;
  protected static String m_XSymbol;
  protected static String m_FSymbol;
  protected static String m_trueSymbol;
  protected static String m_falseSymbol;
  protected static String m_greaterThanSymbol;
  
  public DotVisitor()
  {
    super();
    m_sb = new StringBuilder();
    m_nodeIds = new Stack<Integer>();
    m_nodeMarked = new Stack<Boolean>();
    m_nodeCount = 0;
    setSymbols();
  }
  
  public final void setColor(boolean b)
  {
    m_inColor = b;
  }
  
  protected abstract void setSymbols();
  
  public String toDot()
  {
    return toDot("");
  }
  
  public String toDot(String title)
  {
    StringBuilder out = new StringBuilder();
    out.append("digraph G {\n");
    out.append("  node [shape=rect,height=\"0.25\",width=\"0.3\",margin=\"0.05,0.05\"];\n");
    out.append(m_sb);
    if (!title.isEmpty())
    {
      out.append("  labelloc=\"t\";\n");
      out.append("  label=\"").append(title).append("\";\n");
    }
    out.append("}");
    return out.toString();    
  }

  @Override
  public void visit(MonitorAnd m)
  {
    popBinary(m_andSymbol, m);
  }

  @Override
  public void visit(MonitorEquals m)
  {
    Integer n = ++m_nodeCount;
    m_sb.append("  ").append(n).append(formatNode("=", m));
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
      m_nodeMarked.push(true);
    else
      m_nodeMarked.push(false);
  }

  @Override
  public void visit(MonitorExists m)
  {
    popQuantifier(m_existsSymbol + m.getVariable() + m_inSymbol + m.getPath(), m, m_orSymbol);
  }

  @Override
  public void visit(MonitorF m)
  {
    popNary(m_FSymbol, m, m_orSymbol, m.getInstances().size());
  }

  @Override
  public void visit(MonitorFalse m)
  {
    Integer n = ++m_nodeCount;
    Monitor mt = new MonitorFalse();
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
    {
      m_nodeMarked.push(true);
      mt.markForDeletion();
    }
    else
    {
      m_nodeMarked.push(false);
    }
    m_sb.append("  ").append(n).append(formatNode(m_falseSymbol, mt));
  }

  @Override
  public void visit(MonitorForAll m)
  {
    popQuantifier(m_forAllSymbol + m.getVariable() + " " + m_inSymbol + " " + m.getPath(), m, m_andSymbol);
  }

  @Override
  public void visit(MonitorG m)
  {
    popNary(m_GSymbol, m, m_andSymbol, m.getInstances().size());
  }

  @Override
  public void visit(MonitorGreaterThan m)
  {
    Integer n = ++m_nodeCount;
    m_sb.append("  ").append(n).append(formatNode(m_greaterThanSymbol, m));
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
      m_nodeMarked.push(true);
    else
      m_nodeMarked.push(false);
  }

  @Override
  public void visit(MonitorImplies m)
  {
    popBinary(m_impliesSymbol, m);
  }

  @Override
  public void visit(MonitorOr m)
  {
    popBinary(m_orSymbol, m);
  }

  @Override
  public void visit(MonitorNot m)
  {
    popUnary(m_notSymbol, m);
  }

  @Override
  public void visit(MonitorTrue m)
  {
    Integer n = ++m_nodeCount;
    Monitor mt = new MonitorTrue();
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
    {
      m_nodeMarked.push(true);
      mt.markForDeletion();
    }
    else
    {
      m_nodeMarked.push(false);
    }
    m_sb.append("  ").append(n).append(formatNode(m_trueSymbol, mt));
  }

  @Override
  public void visit(MonitorU m)
  {
    popBinary(m_USymbol, m);
  }

  @Override
  public void visit(MonitorX m)
  {
    popUnary(m_XSymbol, m);
  }

  @Override
  public void visit(MonitorXor m)
  {
    popBinary(m_xorSymbol, m);
  }
  
  protected void popUnary(String symbol, Monitor m)
  {
    Integer right = m_nodeIds.pop();
    Boolean marked = m_nodeMarked.pop();
    Integer n = ++m_nodeCount;
    String edge_style = getLineStyle(m.isMarkedForDeletion() || marked);
    String line_color = getLineColor(m.isMarkedForDeletion() || marked);
    m_sb.append("  ").append(n).append(formatNode(symbol, m));
    m_sb.append("  ").append(n).append(" -> ").append(right).append(" [style=\"").append(edge_style).append("\",color=\"").append(line_color).append("\"];\n");
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
      m_nodeMarked.push(true);
    else
      m_nodeMarked.push(false);
  }
  
  protected void popBinary(String symbol, Monitor m)
  {
    Integer right = m_nodeIds.pop();
    Integer left = m_nodeIds.pop();
    Integer n = ++m_nodeCount;
    Boolean marked_left = m_nodeMarked.pop();
    Boolean marked_right = m_nodeMarked.pop();
    m_sb.append("  ").append(n).append(formatNode(symbol, m));
    {
      String edge_style = getLineStyle(m.isMarkedForDeletion() || marked_left);
      String line_color = getLineColor(m.isMarkedForDeletion() || marked_left);
      m_sb.append("  ").append(n).append(" -> ").append(left).append(" [style=\"").append(edge_style).append("\",color=\"").append(line_color).append("\"];\n");
    }
    {
      String edge_style = getLineStyle(m.isMarkedForDeletion() || marked_right);
      String line_color = getLineColor(m.isMarkedForDeletion() || marked_right);
      m_sb.append("  ").append(n).append(" -> ").append(right).append(" [style=\"").append(edge_style).append("\",color=\"").append(line_color).append("\"];\n");
    }
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
      m_nodeMarked.push(true);
    else
      m_nodeMarked.push(false);
  }
  
  protected void popNary(String symbol, Monitor m, String join_symbol, int arity)
  {
    Integer dummy = ++m_nodeCount;
    for (int i = 0; i < arity; i++)
    {
      Integer under = m_nodeIds.pop();
      Boolean marked = m_nodeMarked.pop();
      String edge_style = getLineStyle(m.isMarkedForDeletion() || marked);
      String line_color = getLineColor(m.isMarkedForDeletion() || marked);
      m_sb.append("  ").append(dummy).append(" -> ").append(under).append(" [style=\"").append(edge_style).append("\",color=\"").append(line_color).append("\"];\n");
    }
    String line_color = getLineColor(m);
    m_sb.append("  ").append(dummy).append(formatDummyNode(join_symbol, m));
    Integer n = ++m_nodeCount;
    m_sb.append("  ").append(n).append(formatNode(symbol, m));
    m_sb.append("  ").append(n).append(" -> ").append(dummy).append(" [dir=none,color=\"").append(line_color).append("\"];\n");
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
      m_nodeMarked.push(true);
    else
      m_nodeMarked.push(false);
  }
  
  protected void popQuantifier(String symbol, MonitorQuantifier m, String join_symbol)
  {
    Integer dummy = ++m_nodeCount;
    List<MonitorQuantifier.ValuePair> instances = m.getInstances();
    ListIterator<MonitorQuantifier.ValuePair> li = instances.listIterator(instances.size());
    // We iterate backwards, to match the instances with the node IDs
    // that we pop from the stack
    while (li.hasPrevious()) 
    {
      MonitorQuantifier.ValuePair vp = li.previous();
      String val = vp.getValue().toString();
      Integer under = m_nodeIds.pop();
      Boolean marked = m_nodeMarked.pop();
      String edge_style = getLineStyle(m.isMarkedForDeletion() || marked);
      String line_color = getLineColor(m.isMarkedForDeletion() || marked);
      m_sb.append("  ").append(dummy).append(" -> ").append(under).append(" [label=\"").append(m.getVariable()).append("=").append(val).append("\",style=\"").append(edge_style).append("\",color=\"").append(line_color).append("\"];\n");
    }
    String line_color = getLineColor(m.isMarkedForDeletion());
    m_sb.append("  ").append(dummy).append(formatDummyNode(join_symbol, m));
    Integer n = ++m_nodeCount;
    m_sb.append("  ").append(n).append(formatNode(symbol, m));
    m_sb.append("  ").append(n).append(" -> ").append(dummy).append(" [dir=none,color=\"").append(line_color).append("\"];\n");
    m_nodeIds.push(n);
    if (m.isMarkedForDeletion())
      m_nodeMarked.push(true);
    else
      m_nodeMarked.push(false);
  }
  
  protected abstract String formatNode(String symbol, Monitor m);
  
  protected abstract String formatDummyNode(String join_symbol, Monitor m);
  
  protected abstract String formatVerdict(Monitor m);
  
  protected String getColor(Monitor m)
  {
    Monitor.Verdict v = m.getVerdict();
    String fill_color = "#FFFFFF"; // White
    if (m_inColor)
    {
      if (v == Monitor.Verdict.FALSE && !m.isMarkedForDeletion())
        fill_color = "#FF3300";
      else if (v == Monitor.Verdict.FALSE && m.isMarkedForDeletion())
        fill_color = "#FFEEBB";
      else if (v == Monitor.Verdict.TRUE && !m.isMarkedForDeletion())
        fill_color = "#99FF00";
      else if (v == Monitor.Verdict.TRUE && m.isMarkedForDeletion())
        fill_color = "#EEFFBB";
      else if (!m.isMarkedForDeletion())
        fill_color = "#FFFF99";
      else
        fill_color = "#FFFFEE";
    }
    return fill_color;
  }
  
  protected static String getLineStyle(Monitor m)
  {
    return getLineStyle(m.isMarkedForDeletion());
  }
  
  protected static String getLineStyle(boolean isMarked)
  {
    if (isMarked)
      return "dashed";
    return "solid";
  }
  
  protected String getLineColor(Monitor m)
  {
    return getLineColor(m.isMarkedForDeletion());
  }
  
  protected String getLineColor(boolean isMarked)
  {
    String color = "#000000";
    if (m_inColor && isMarked)
      color = "#AAAAAA";
    return color;
  }

}
