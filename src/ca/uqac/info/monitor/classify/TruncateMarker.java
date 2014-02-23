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

public class TruncateMarker extends PrefixMarker
{
  /**
   * The depth in the monitor tree under which all nodes will
   * be marked for deletion
   */
  protected int m_maxDepth = 2;
  
  public TruncateMarker()
  {
    super();
  }
  
  public TruncateMarker(int depth)
  {
    this();
    m_maxDepth = depth;
  }
  
  @Override
  protected void mark(Monitor m, boolean to_delete, boolean except_yourself)
  {
    if (m_operators.size() >= m_maxDepth)
      to_delete = true;
    super.mark(m, to_delete, except_yourself);
  }
}
