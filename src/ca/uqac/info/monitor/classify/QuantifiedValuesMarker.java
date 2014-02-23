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

import ca.uqac.info.ltl.Constant;
import ca.uqac.info.monitor.*;

/**
 * This marker removes all values of quantified variables. This deletion
 * is actually simulated by setting all values to the same symbol.
 * @author sylvain
 *
 */
public class QuantifiedValuesMarker extends PrefixMarker
{
  protected static final Constant m_constant = new Constant("?");
  
  @Override
  protected void mark(MonitorForAll m, boolean to_delete, boolean except_yourself)
  {
    for (MonitorQuantifier.ValuePair vp : m.getInstances())
    {
      vp.setValue(m_constant);
      mark(vp.getMonitor(), to_delete, except_yourself);
    }
  }
  
  @Override
  protected void mark(MonitorExists m, boolean to_delete, boolean except_yourself)
  {
    for (MonitorQuantifier.ValuePair vp : m.getInstances())
    {
      vp.setValue(m_constant);
      mark(vp.getMonitor(), to_delete, except_yourself);
    }
  }
}