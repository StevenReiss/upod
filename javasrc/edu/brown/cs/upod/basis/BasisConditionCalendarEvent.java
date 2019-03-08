/********************************************************************************/
/*										*/
/*		BasisConditionGoogleCalendar.java				*/
/*										*/
/*	Condition based on google calendar events				*/
/*										*/
/********************************************************************************/
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

import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.*;

import java.util.*;


public class BasisConditionCalendarEvent extends BasisCondition implements BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<FieldMatch> field_matches;
private String		 condition_name;

private static BasisCalendarChecker cal_checker = new BasisCalendarChecker();


enum NullType { EITHER, NULL, NONNULL };
enum MatchType { IGNORE, MATCH, NOMATCH };


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisConditionCalendarEvent(UpodProgram pgm,Element xml)
{
   super(pgm,xml);

   field_matches = new ArrayList<FieldMatch>();

   condition_name = IvyXml.getAttrString(xml,"NAME");
   for (Element fe : IvyXml.children(xml,"FIELD")) {
      FieldMatch fm = new FieldMatch(fe);
      field_matches.add(fm);
    }

   cal_checker.addCondition(this);
}


BasisConditionCalendarEvent(UpodProgram pgm,String name)
{
   super(pgm.getUniverse());

   field_matches = new ArrayList<FieldMatch>();

   condition_name = name;

   cal_checker.addCondition(this);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

void addFieldMatch(String name,String val)
{
   FieldMatch fm = new FieldMatch(name,NullType.EITHER,MatchType.MATCH,val);
   field_matches.add(fm);
}


@Override public String getDescription()
{
   // should get field data here
   return condition_name;
}


@Override public String getLabel()
{
   String s= super.getLabel();
   if (s == null) s = condition_name;
   // get default description here
   return s;
}


@Override public String getName()
{
   return condition_name;
}

@Override public void getSensors(Collection<UpodDevice> rslt)	{ }




/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

@Override public void setTime(UpodWorld w)
{
   BasisGoogleCalendar bc = BasisGoogleCalendar.getCalendar(w);
   if (bc == null) return;
   String evt = getEventString();
   Map<String,String> rslt = new HashMap<String,String>();
   if (bc.findEvent(w.getTime(),evt,rslt)) {
      BasisPropertySet prms = new BasisPropertySet();
      for (Map.Entry<String,String> ent : rslt.entrySet()) {
	 prms.put(ent.getKey(),ent.getValue());
       }
      BasisLogger.logI("CONDITION " + getLabel() + " ON");
      fireOn(w,prms);
    }
   else{
      BasisLogger.logI("CONDITION " + getLabel() + " OFF");
      fireOff(w);
    }
}


private String getEventString()
{
   StringBuffer buf = new StringBuffer();
   for (FieldMatch fm : field_matches) {
      String s = fm.toPattern();
      if (s == null || s.length() == 0) continue;
      if (buf.length() > 0) buf.append(",");
      buf.append(s);
    }
   return buf.toString();
}




/********************************************************************************/
/*										*/
/*	Overlap checking							*/
/*										*/
/********************************************************************************/

protected boolean isConsistentWith(BasisCondition bc)
{
   if (!(bc instanceof BasisConditionCalendarEvent)) return true;

   // check if two calendar events can overlap
   return true;
}


@Override public void addImpliedProperties(UpodPropertySet ups)
{
   // need to add special calendar properties here
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   outputHeader(xw);
   xw.field("TYPE","GOOGLE");

   for (FieldMatch fm : field_matches) {
      fm.outputXml(xw);
    }
   outputTrailer(xw);
}



/********************************************************************************/
/*										*/
/*	Field Match information 						*/
/*										*/
/********************************************************************************/

private static class FieldMatch {

   private String   field_name;
   private NullType null_type;
   private MatchType match_type;
   private List<String> match_values;

   FieldMatch(Element xml) {
      field_name = IvyXml.getAttrString(xml,"NAME").trim();
      null_type = IvyXml.getAttrEnum(xml,"NULL",NullType.EITHER);
      match_type = IvyXml.getAttrEnum(xml,"MATCH",MatchType.IGNORE);
      match_values = new ArrayList<String>();
      String txt = IvyXml.getAttrString(xml,"MATCHTEXT");
      if (txt == null) {
	 for (Element e : IvyXml.children(xml,"MATCHVALUE")) {
	    String t = IvyXml.getText(e);
	    if (t == null) t = IvyXml.getAttrString(e,"VALUE");
	    StringTokenizer tok = new StringTokenizer(t,",| ");
	    while (tok.hasMoreTokens()) {
	       match_values.add(tok.nextToken());
	     }
	  }
       }
      else {
	 StringTokenizer tok = new StringTokenizer(txt,",| ");
	 while (tok.hasMoreTokens()) {
	    match_values.add(tok.nextToken());
	  }
       }
    }

   FieldMatch(String name,NullType ntyp,MatchType mtyp,String txt) {
      field_name = name;
      null_type = ntyp;
      match_type = mtyp;
      match_values = new ArrayList<String>();
      StringTokenizer tok = new StringTokenizer(txt,", ");
      while (tok.hasMoreTokens()) {
	 match_values.add(tok.nextToken());
       }
    }

   String toPattern() {
      StringBuffer buf = new StringBuffer();
      switch (null_type) {
	 case EITHER :
	    break;
	 case NULL :
	    buf.append("!");
	    buf.append(field_name);
	    break;
	 case NONNULL :
	    buf.append(field_name);
	    break;
       }
      if (!match_values.isEmpty()) {
	 if (buf.length() > 0) buf.append(",");
	 buf.append(field_name);
	 if (match_type == MatchType.NOMATCH) buf.append("!");
	 else buf.append("=");
	 int ctr = 0;
	 for (String s : match_values) {
	    if (ctr++ > 0) buf.append("|");
	    buf.append(s);
	  }
       }
      return buf.toString();
    }

   void outputXml(IvyXmlWriter xw) {
      xw.begin("FIELD");
      xw.field("NAME",field_name.trim());
      xw.field("NULL",null_type);
      xw.field("MATCH",match_type);
      StringBuffer buf = new StringBuffer();
      for (String s : match_values) {
	 if (buf.length() > 0) buf.append(",");
	 buf.append(s);
       }
      if (buf.length() > 0) xw.textElement("MATCHVALUE",buf.toString());
      xw.end("FIELD");
    }

}	// end of inner class FieldMatch




}	// end of class BasisConditionGoogleCalendar




/* end of BasisConditionGoogleCalendar.java */

