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

/**
 * Interface for the Visitor pattern applied to monitors.
 * @author sylvain
 */
public interface MonitorVisitor
{
  public void visit(MonitorAnd m);
  public void visit(MonitorEquals m);
  public void visit(MonitorExists m);
  public void visit(MonitorF m);
  public void visit(MonitorFalse m);
  public void visit(MonitorForAll m);
  public void visit(MonitorG m);
  public void visit(MonitorGreaterThan m);
  public void visit(MonitorImplies m);
  public void visit(MonitorOr m);
  public void visit(MonitorNot m);
  public void visit(MonitorTrue m);
  public void visit(MonitorU m);
  public void visit(MonitorX m);
  public void visit(MonitorXor m);
}
