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

import ca.uqac.info.monitor.*;

/**
 * The fail-fast marker tags for deletion all nodes of the hologram
 * that are not necessary to determine the value of a formula
 * @author sylvain
 *
 */
public class FailFastMarker extends PrefixMarker
{
  @Override
  protected void mark(MonitorG m, boolean to_delete, boolean except_yourself)
  {
    List<Monitor> mons = m.getInstances();
    if (to_delete)
    {
      m.markForDeletion();
    }
    for (Monitor mon : mons)
    {
      if (to_delete)
        mark(mon, to_delete, except_yourself);
      else if (mon.getVerdict() == Monitor.Verdict.FALSE)
        to_delete = true;
    }
  }
  
  @Override
  protected void mark(MonitorF m, boolean to_delete, boolean except_yourself)
  {
    List<Monitor> mons = m.getInstances();
    if (to_delete)
    {
      m.markForDeletion();
    }
    for (Monitor mon : mons)
    {
      if (to_delete)
        mark(mon, to_delete, except_yourself);
      else if (mon.getVerdict() == Monitor.Verdict.TRUE)
        to_delete = true;
    }
  }
}
