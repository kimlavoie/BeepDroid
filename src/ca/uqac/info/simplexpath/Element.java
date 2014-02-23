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
package ca.uqac.info.simplexpath;

import java.util.*;

/**
 * Representation of an XML document as a tree of named elements.
 * The current implementation of this class handles XML documents,
 * barring these few restrictions:
 * <ul>
 * <li>Elements may have attributes, but they are ignored</li>
 * <li>Self-closing elements are not recognized</li>
 * <li>The schema of the document may not be recursive, i.e. an
 *   element named <tt>&lt;a&gt;</tt> must not <em>contain</em> another element
 *   named <tt>&lt;a&gt;</tt> at any level of nesting</li>
 * <li>Entities and CDATA sections are not supported</li>
 * </ul>
 * An Element can be built from a String by using {@link Element.parse},
 * and then queried using XPath by creating some {@link SimpleXPathExpression}
 * <tt>exp</tt> and calling {@link SimpleXPathExpression.evaluate}.
 * @author sylvain
 *
 */
public class Element
{
  String m_name;
  List<Element> m_children;
  boolean m_isLeaf = false;
  //protected static final Pattern m_tagPattern = Pattern.compile("^<([\\w:]+?)\\s*?[^>]*?>(.*?)<\\s*?/\\s*?\\1\\s*?>", Pattern.MULTILINE + Pattern.DOTALL);
  
  public Element()
  {
    super();
    m_name = "";
    m_children = new LinkedList<Element>();
  }
  
  public static Element parse(String s) throws ParseException
  {
    List<Element> out = parseChildren(s);
    if (!out.isEmpty())
    {
      for (Element e : out)
      {
        return e;
      }
    }
    return null;
  }
  
  /*protected static List<Element> parseChildren(String s)
  {
    s = s.trim();
    List<Element> out_set = new LinkedList<Element>();
    Matcher match = m_tagPattern.matcher(s);
    while (!s.isEmpty())
    {
      Element new_e = new Element();
      if (match.find())
      {
        new_e.m_name = match.group(1);
        new_e.m_children = Element.parseChildren(match.group(2));
        int end = match.end();
        s = s.substring(end).trim();
        match = m_tagPattern.matcher(s);          
      }
      else
      {
        new_e.m_name = s;
        new_e.m_isLeaf = true;
        s = "";
      }
      out_set.add(new_e);
    }
    return out_set;
  }*/
  
  protected static List<Element> parseChildren(String s) throws ParseException
  {
    List<Element> out_set = new LinkedList<Element>();
    s = s.trim();
    while (!s.isEmpty())
    {
      Element new_e = new Element();
      int left_beg = s.indexOf("<");
      if (left_beg >= 0)
      {
        int left_end = s.indexOf(">");
        if (left_end < 0 || left_end < left_beg)
          throw new ParseException("Error finding end of tag");
        String tagname = s.substring(left_beg + 1, left_end);
        int space_pos = tagname.indexOf(" ");
        if (space_pos > 0)
        {
          tagname = tagname.substring(0, space_pos);
        }
        int right_beg = s.indexOf("</" + tagname + ">");
        if (right_beg < 0 || right_beg < left_end)
          throw new ParseException("Error finding end tag for " + tagname);
        String child_contents = s.substring(left_end + 1, right_beg);
        new_e.m_name = tagname;
        new_e.m_children = Element.parseChildren(child_contents);
        s = s.substring(right_beg + tagname.length() + 3).trim();
      }
      else
      {
        new_e.m_name = s;
        new_e.m_isLeaf = true;
        s = "";        
      }
      out_set.add(new_e);
    }
    return out_set;
  }
  
  @Override
  public String toString()
  {
    return toStringBuilder(new StringBuilder()).toString();
  }
  
  protected StringBuilder toStringBuilder(StringBuilder indent)
  {
    StringBuilder out = new StringBuilder();
    if (m_isLeaf)
    {
      out.append(indent).append(m_name);
    }
    else
    {
      out.append(indent).append("<").append(m_name).append(">\n");
      StringBuilder n_indent = new StringBuilder(indent).append(" ");
      for (Element e : m_children)
      {
        out.append(e.toStringBuilder(n_indent));
        if (e.m_isLeaf)
          out.append("\n");
      }
      out.append(indent).append("</").append(m_name).append(">\n");
    }
    return out;
  }
  
  public static void main(String[] args)
  {
    try
    {
      Element e = Element.parse("<root>\n<abc>ddd</abc><abc>ded</abc></root>");
      System.out.println(e);
    }
    catch (ParseException e)
    {
      
    }
  }
  
  /**
   * Simple class representing an error in the parsing of an XML document
   * @author sylvain
   */
  public static class ParseException extends Exception
  {
    /**
     * Dummy UID
     */
    private static final long serialVersionUID = 1L;
    
    protected String m_message;
    
    public ParseException(String message)
    {
      super();
      m_message = message;
    }
    
    @Override
    public String toString()
    {
      return m_message;
    }
  }
}