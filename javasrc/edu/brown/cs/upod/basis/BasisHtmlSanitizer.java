/********************************************************************************/
/*										*/
/*		BasisHtmlSanitizer.java 					*/
/*										*/
/*	Sanitize HTML to prevent CSS attacks					*/
/*										*/
/********************************************************************************/
/*	Derived from nu.dll.ap.weblatte.HtmlSanitizer			      */
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.upod.basis;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

import java.util.regex.*;
import java.util.*;
import java.io.*;



public class BasisHtmlSanitizer extends HTMLEditorKit.ParserCallback {



/********************************************************************************/
/*										*/
/*	Constant definitions							*/
/*										*/
/********************************************************************************/

private HTML.Attribute[] alwaysAllowedAttributesArray = {
   HTML.Attribute.BGCOLOR,
   HTML.Attribute.STYLE,
   HTML.Attribute.CLASS,
   HTML.Attribute.TITLE,
   HTML.Attribute.WIDTH,
   HTML.Attribute.HEIGHT,
   HTML.Attribute.ALIGN,
   HTML.Attribute.NOSHADE,
   HTML.Attribute.BORDER,
   HTML.Attribute.ID,
   HTML.Attribute.NAME
};

private HTML.Tag[] strippedTagsArray = {
   HTML.Tag.SCRIPT, HTML.Tag.A, HTML.Tag.IMG,
   HTML.Tag.LINK, HTML.Tag.STYLE
};

private HTML.Tag[] allowedTagsArray = {
   HTML.Tag.ADDRESS,
   HTML.Tag.B,
   HTML.Tag.BASEFONT,
   HTML.Tag.BIG,
   HTML.Tag.BLOCKQUOTE,
   HTML.Tag.BODY,
   HTML.Tag.BR,
   HTML.Tag.CAPTION,
   HTML.Tag.CENTER,
   HTML.Tag.CODE,
   HTML.Tag.DIV,
   HTML.Tag.EM,
   HTML.Tag.FONT,
   HTML.Tag.H1,
   HTML.Tag.H2,
   HTML.Tag.H3,
   HTML.Tag.H4,
   HTML.Tag.H5,
   HTML.Tag.H6,
   HTML.Tag.HEAD,
   HTML.Tag.HR,
   HTML.Tag.HTML,
   HTML.Tag.I,
   HTML.Tag.LI,
   HTML.Tag.OL,
   HTML.Tag.P,
   HTML.Tag.PRE,
   HTML.Tag.SPAN,
   HTML.Tag.STRONG,
   HTML.Tag.SUB,
   HTML.Tag.SUP,
   HTML.Tag.TABLE,
   HTML.Tag.TD,
   HTML.Tag.TH,
   HTML.Tag.TITLE,
   HTML.Tag.TR,
   HTML.Tag.U,
   HTML.Tag.UL
};


static private Pattern dqEscPattern = Pattern.compile("\"");



/********************************************************************************/
/*										*/
/*	Local Storage								*/
/*										*/
/********************************************************************************/

private Set<HTML.Tag> allowedTags = new HashSet<HTML.Tag>(Arrays.asList(allowedTagsArray));
private Set<HTML.Tag> strippedTags = new HashSet<HTML.Tag>(Arrays.asList(strippedTagsArray));
private Set<HTML.Attribute> alwaysAllowedAttributes = new HashSet<HTML.Attribute>(Arrays.asList(alwaysAllowedAttributesArray));
private Writer out_writer;
private HTML.Tag lastOpenedTag = null;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisHtmlSanitizer(Writer os) throws IOException
{
   out_writer = os;
}



/********************************************************************************/
/*										*/
/*	HTML Parsing methods							*/
/*										*/
/********************************************************************************/

@Override public void flush()
{
   try {
      out_writer.flush();
    }
   catch (IOException ex1) {
      throw new RuntimeException(ex1.toString(), ex1);
    }
}



@Override public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attrs, int pos)
{
   handleTag(tag, attrs, pos);
}


@Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrs, int pos)
{
   handleTag(tag, attrs, pos);
}


private void handleTag(HTML.Tag tag, MutableAttributeSet attrs, int pos)
{
   lastOpenedTag = tag;
   try {
      if (!allowedTags.contains(tag)) {
	 return;
       }

      Map<HTML.Attribute,Object> attributes = new HashMap<HTML.Attribute,Object>();
      Enumeration<?> e = attrs.getAttributeNames();
      while (e.hasMoreElements()) {
	 Object o = e.nextElement();
	 if (o == IMPLIED) {
	    continue;
	  }
	 if (o == HTML.Attribute.ENDTAG) {
	    out_writer.write("</");
	    out_writer.write(tag.toString());
	    out_writer.write(">");
	    continue;
	  }
	 HTML.Attribute attribute = null;
	 if (o instanceof String) { }
	 else if (o instanceof HTML.Attribute) { }
	 else {
	    continue;
	  }
	 Object value = attrs.getAttribute(o);
	 if (alwaysAllowedAttributes.contains(o)) {
	    attributes.put(attribute, value);
	    continue;
	  }

	if (tag == HTML.Tag.BODY) {
	    if (attribute == HTML.Attribute.BACKGROUND ||
		   attribute == HTML.Attribute.BGCOLOR) {
	       attributes.put(attribute, value);
	     }
	  }
	 else if (tag == HTML.Tag.FONT) {
	    if (attribute == HTML.Attribute.SIZE ||
		   attribute == HTML.Attribute.COLOR ||
		   attribute == HTML.Attribute.FACE) {
	       attributes.put(attribute, value);
	     }
	  }
	 else if (tag == HTML.Tag.TD ||
		     tag == HTML.Tag.TR ||
		     tag == HTML.Tag.TH) {
	    if (attribute == HTML.Attribute.ROWSPAN ||
		   attribute == HTML.Attribute.COLSPAN) {
	       attributes.put(attribute, value);
	     }
	  }
       }
      out_writer.write("<" + tag.toString());
      for (Iterator<Map.Entry<HTML.Attribute,Object>> i = attributes.entrySet().iterator();
	 i.hasNext();) {
	 Map.Entry<HTML.Attribute,Object> entry = i.next();
	 out_writer.write(" ");
	 out_writer.write(entry.getKey().toString());
	 out_writer.write("=\"");
	 if (entry.getValue() != HTML.NULL_ATTRIBUTE_VALUE)
	    out_writer.write(dquote(entry.getValue().toString()));
	 out_writer.write("\"");
       }
      out_writer.write(">");
    }
   catch (IOException ex1) {
      throw new RuntimeException(ex1.toString(), ex1);
    }
}




@Override public void handleText(char[] data, int pos)
{
   try {
      /* For some reason "<br />" translates to:
	 * handleSimpleTag(<BR>) -> handleText(">" + trailing text)
	 * This is true for all tags, so we simple remove any
	 * introducing ">"
	 */
      if (data.length == 1 && data[0] == '>') {
	 return;
       }
      String dataString;
      if (data.length > 1 && data[0] == '>') {
	 dataString = new String(data, 1, data.length-1);
       }
      else {
	 dataString = new String(data);
       }
      if (strippedTags.contains(lastOpenedTag)) {
	 return;
       }
      dataString = dataString.replaceAll("<", "&lt;");
      data = dataString.toCharArray();
      out_writer.write(data);
    }
   catch (IOException ex1) {
      throw new RuntimeException(ex1.toString(), ex1);
    }
}




@Override public void handleEndTag(HTML.Tag t, int pos)
{
   try {
      if (allowedTags.contains(t)) {
	 out_writer.write("</");
	 out_writer.write(t.toString());
	 out_writer.write(">");
       }
    }
   catch (IOException ex1) {
      throw new RuntimeException(ex1.toString(), ex1);
    }
}



private static String dquote(String s)
{
   return dqEscPattern.matcher(s).replaceAll("'");
}



/********************************************************************************/
/*										*/
/*	Actual action methods							*/
/*										*/
/********************************************************************************/

public static void parse(Reader is,Writer os)
	throws IOException
{
   BasisHtmlSanitizer hs = new BasisHtmlSanitizer(os);
   ParserDelegator pd = new ParserDelegator();
   pd.parse(is, hs, true);
   hs.flush();
}


public static String sanitize(String s)
{
   try {
      StringWriter sw = new StringWriter();
      BasisHtmlSanitizer hs = new BasisHtmlSanitizer(sw);
      ParserDelegator pd = new ParserDelegator();
      pd.parse(new StringReader(s),hs,true);
      hs.flush();
      return sw.toString();
    }
   catch (IOException e) {
      return null;
    }
}


}	// end of class BasisHtmlSanitizer




/* end of BasisHtmlSanitizer.java */
