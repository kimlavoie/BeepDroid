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

import ca.uqac.info.monitor.Monitor;

public class DotHtmlVisitor extends DotVisitor
{
  @Override
  protected void setSymbols()
  {
    m_forAllSymbol = "∀";
    m_existsSymbol = "∃";
    m_inSymbol = "∈";
    m_andSymbol = "∧";
    m_orSymbol = "∨";
    m_xorSymbol = "⊕";
    m_impliesSymbol = "→";
    m_notSymbol = "¬";
    m_GSymbol = "<b>G</b>";
    m_USymbol = "<b>U</b>";
    m_XSymbol = "<b>X</b>";
    m_FSymbol = "<b>F</b>";
    m_trueSymbol = "⊤";
    m_falseSymbol = "⊥";
    m_greaterThanSymbol = "&gt;";
  }
  
  @Override
  protected String formatNode(String symbol, Monitor m)
  {
    String fill_color = getColor(m);
    String line_style = getLineStyle(m);
    String line_color = getLineColor(m);
    return " [label=<" + symbol + formatVerdict(m) + ">,shape=rect,style=\"filled," + line_style + "\",height=0.25,fillcolor=\"" + fill_color + "\",color=\"" + line_color + "\"];\n";
  }
  
  @Override
  protected String formatDummyNode(String join_symbol, Monitor m)
  {
    StringBuilder out = new StringBuilder();
    String line_color = getLineColor(m);
    String line_style = getLineStyle(m);
    out.append(" [style=\"filled,").append(line_style).append("\",fillcolor=\"").append(getColor(m)).append("\",label=<").append(join_symbol).append(">,shape=circle,height=0.2,fixedsize=true,color=\"").append(line_color).append("\"];\n");
    return out.toString();
  }
  
  @Override
  protected String formatVerdict(Monitor m)
  {
    Monitor.Verdict v = m.getVerdict();
    if (v == Monitor.Verdict.TRUE)
      return "<sup>" + m_trueSymbol + "</sup>";
    if (v == Monitor.Verdict.FALSE)
      return "<sup>" + m_falseSymbol + "</sup>";
    return "<sup>?</sup>";
  }
}
