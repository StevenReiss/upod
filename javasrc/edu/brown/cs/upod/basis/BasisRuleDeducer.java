/********************************************************************************/ /*										   */
/*		BasisRuleDeducer.java						*/
/*										*/
/*	Handle automatic deduction of new rules based on settings		*/
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

import org.w3c.dom.Element;

import java.util.*;
import java.io.*;


public class BasisRuleDeducer implements BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private long	at_time;
private UpodWorld from_world;
private UpodProgram for_program;
private Map<UpodDevice,Map<UpodParameter,Object>> sensor_values;
private Map<UpodDevice,Map<UpodParameter,Object>> entity_values;
private Collection<CalendarEvent> calendar_events;
private TreeNode tree_root;

private static final long EVENT_START_LEEWAY = 10*T_MINUTE;
private static final long EVENT_DEFAULT_LENGTH = T_HOUR;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisRuleDeducer(UpodWorld w,UpodProgram pgm,
      Map<UpodDevice,Map<UpodParameter,Object>> smap,
      Map<UpodDevice,Map<UpodParameter,Object>> emap)
{
   at_time = w.getTime();
   from_world = w;
   for_program = pgm;
   sensor_values = smap;
   entity_values = emap;
   BasisGoogleCalendar bcg = BasisGoogleCalendar.getCalendar(w);
   calendar_events = bcg.getActiveEvents(at_time);
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

public synchronized void outputRule() throws IOException
{
   File f1 = for_program.getUniverse().getBaseDirectory();
   File f2 = new File(f1,RULE_FILE);
   FileWriter fw = new FileWriter(f2,true);
   IvyXmlWriter xw = new IvyXmlWriter(fw);

   xw.begin("RULE");
   xw.field("WHEN",at_time);
   xw.field("WHENS",new Date(at_time).toString());

   xw.begin("WORLD");
   for (Map.Entry<UpodParameter,Object> ent : from_world.getParameters().entrySet()) {
      UpodParameter pnm = ent.getKey();
      Object pvl = ent.getValue();
      if (pvl == null) continue;
      xw.begin("PARAM");
      xw.field("NAME",pnm.getName());
      xw.field("TYPE",pvl.getClass().toString());
      xw.cdata(pvl.toString());
      xw.end("PARAM");
    }
   xw.end("WORLD");

   for (Map.Entry<UpodDevice,Map<UpodParameter,Object>> ent : sensor_values.entrySet()) {
      UpodDevice us = ent.getKey();
      xw.begin("SENSOR");
      xw.field("NAME",us.getName());
      xw.field("ID",us.getUID());
      for (Map.Entry<UpodParameter,Object> pent : ent.getValue().entrySet()) {
	 xw.begin("PARAM");
	 xw.field("NAME",pent.getKey().getName());
	 xw.cdata(pent.getValue().toString());
	 xw.end("PARAM");
       }
      xw.end("SENSOR");
    }
   for (Map.Entry<UpodDevice,Map<UpodParameter,Object>> ent : entity_values.entrySet()) {
      UpodDevice ue = ent.getKey();
      xw.begin("ENTITY");
      xw.field("NAME",ue.getName());
      xw.field("ID",ue.getUID());
      for (Map.Entry<UpodParameter,Object> pent : ent.getValue().entrySet()) {
	 xw.begin("PARAM");
	 xw.field("NAME",pent.getKey().getName());
	 xw.cdata(pent.getValue().toString());
	 xw.end("PARAM");
       }
      xw.end("ENTITY");
    }

   for (CalendarEvent ce : calendar_events) {
      if (ce.getStartTime() > at_time + 5*T_MINUTE) continue;
      if (ce.getEndTime() < at_time - 5*T_MINUTE) continue;
      xw.begin("CALENDAR");
      xw.field("START",ce.getStartTime());
      xw.field("STARTS",new Date(ce.getStartTime()).toString());
      xw.field("END",ce.getEndTime());
      xw.field("ENDS",new Date(ce.getEndTime()).toString());
      for (Map.Entry<String,String> ent : ce.getProperties().entrySet()) {
	 xw.begin("FIELD");
	 xw.field("NAME",ent.getKey());
	 xw.field("VALUE",ent.getValue());
	 xw.end("FIELD");
       }
      xw.end("CALENDAR");
    }
   xw.end("RULE");
   xw.close();
}



/********************************************************************************/
/*										*/
/*	Code to deduce a new set of rules					*/
/*										*/
/********************************************************************************/

public void deduceRules()
{
   List<RuleInstance> allrules = loadRules();

   List<UpodRule> rules = new ArrayList<UpodRule>();
   for (UpodRule ur : for_program.getRules()) {
      if (ur.isExplicit()) {
	 rules.add(ur);
	 RuleInstance ri = new RuleInstance(ur,from_world.getUniverse());
	 allrules.add(ri);
       }
    }

   Set<Condition> allconds = getConditions(allrules);

   tree_root = new TreeNode(allrules,null);

   expandTree(tree_root,allconds);

   generateRules(tree_root);
}


private List<RuleInstance> loadRules()
{
   List<RuleInstance> allrules = new ArrayList<RuleInstance>();

   try {
      File f1 = for_program.getUniverse().getBaseDirectory();
      File f2 = new File(f1,RULE_FILE);
      FileInputStream fis = new FileInputStream(f2);
      IvyXmlReader xr = new IvyXmlReader(fis);
      for ( ; ; ) {
	 String xmls = xr.readXml();
	 if (xmls == null) break;
	 Element xml = IvyXml.convertStringToXml(xmls);
	 RuleInstance ri = new RuleInstance(xml,from_world.getUniverse());
	 allrules.add(ri);
       }
      xr.close();
    }
   catch (IOException e) {
      BasisLogger.logE("Problem reading rules",e);
    }

   return allrules;
}



/********************************************************************************/
/*										*/
/*	Build the set of conditions						*/
/*										*/
/********************************************************************************/

private Set<Condition> getConditions(List<RuleInstance> rules)
{
   Set<Condition> conds = new HashSet<Condition>();

   getTimeConditions(rules,conds);
   getSensorConditions(rules,conds);
   getCalendarConditions(rules,conds);

   return null;
}



/********************************************************************************/
/*										*/
/*	Handle time conditions							*/
/*										*/
/********************************************************************************/

private void getTimeConditions(List<RuleInstance> rules,Set<Condition> conds)
{
   for (RuleInstance ri : rules) {
      getTimeConditions(ri,rules,conds);
    }
}


private void getTimeConditions(RuleInstance ri,List<RuleInstance> allrules,Set<Condition> conds)

{
   Calendar cal = Calendar.getInstance();
   cal.setTimeInMillis(ri.getTime());

   long when = ri.getTime();
   when = roundTime(when);
   BitSet days = new BitSet();
   long whentime = dayTime(when);
   int today = cal.get(Calendar.DAY_OF_WEEK);
   days.set(today);

   // find viable end time
   long endt = 0;
   for (CalEvent ce : ri.getEvents()) {
      long st = ce.getStartTime();
      if (Math.abs(st-when) < EVENT_START_LEEWAY) {
	 long et = dayTime(ce.getEndTime());
	 if (endt == 0 || et < endt) et = endt;
	 if (st > when) endt += st-when;

	 // use event recurrence info to update days
       }
    }
   BasisUniverse bu = (BasisUniverse) from_world.getUniverse();
   BasisHistoryDeducer bhd = bu.getHistoryDeducer();
   BasisCalendarEvent bce = bhd.findImpliedEvent(when);
   if (bce != null) {
      // use the calendar event here to set days and
      // possibly the end time
    }

   Calendar xc = Calendar.getInstance();
   RuleInstance ribest = null;
   RuleInstance ribesttoday = null;
   long bestwhen = 0;
   for (RuleInstance xri : allrules) {
      if (xri == ri) continue;
      long xwhen = xri.getTime();
      long xwhentime = dayTime(xwhen);
      xc.setTimeInMillis(xri.getTime());
      int xtoday = xc.get(Calendar.DAY_OF_WEEK);
      if (xtoday == today && xwhentime > whentime) {
	 if (ribesttoday == null || ribesttoday.getTime() > xri.getTime()) {
	    ribesttoday = xri;
	  }
       }
      if (xwhentime > whentime) {
	 if (ribest == null || bestwhen > xwhentime) {
	    ribest = xri;
	    bestwhen = xwhentime;
	  }
       }
    }

   if (ribesttoday != null) {
      long t0 = ribesttoday.getTime();
      long t1 = dayTime(t0);
      if (endt == 0 || endt > t1) endt = t1;
    }
   else {
      if (endt == 0) endt = when + EVENT_DEFAULT_LENGTH;
      if (ribest != null) {
	 long t0 = ribest.getTime();
	 long t1 = dayTime(t0);
	 if (endt == 0 || endt > t1) endt = t1;
       }
    }

   TimeCondition tc = new TimeCondition(whentime,endt,days);
   conds.add(tc);
}




private static long dayTime(long when)
{
   Calendar cal = Calendar.getInstance();
   cal.setTimeInMillis(when);
   int hour = cal.get(Calendar.HOUR_OF_DAY);
   int min = cal.get(Calendar.MINUTE);
   return T_MINUTE*(min + 60*hour);
}


private static long roundTime(long when)
{
   long delta = 5*T_MINUTE;
   long w0 = when / delta;
   w0 *= delta;
   return w0;
}


private static int dayOfWeek(long when)
{
   Calendar cal = Calendar.getInstance();
   cal.setTimeInMillis(when);
   return cal.get(Calendar.DAY_OF_WEEK);
}


/********************************************************************************/
/*										*/
/*	Handle sensor conditions						*/
/*										*/
/********************************************************************************/

private void getSensorConditions(List<RuleInstance> rules,Set<Condition> conds)
{
   for (int i = 0; i < rules.size(); ++i) {
      RuleInstance ri1 = rules.get(i);
      for (int j = i+1; j < rules.size(); ++j) {
	 RuleInstance ri2 = rules.get(j);
	 if (ri1.getStateId() == ri2.getStateId()) continue;
	 addSensorConditions(ri1,ri2,conds);
       }
    }
}



private void addSensorConditions(RuleInstance r1,RuleInstance r2,Set<Condition> rslt)
{
   for (String sid : r1.getSensors()) {
      String s1 = r1.getSensor(sid);
      String s2 = r2.getSensor(sid);
      if (!s1.equals(s2)) {
	 SensorCondition sc = new SensorCondition(sid,s1);
	 rslt.add(sc);
	 sc = new SensorCondition(sid,s2);
	 rslt.add(sc);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Handle calendar conditions						*/
/*										*/
/********************************************************************************/

private static final String [] cal_props = new String [] {
   "ID", "WHERE", "WHO", "TRANSPARENCY", "CONTENT"
};


private void getCalendarConditions(List<RuleInstance> rules,Set<Condition> conds)
{
   boolean havenone = false;

   List<CalEvent> evts = new ArrayList<CalEvent>();
   for (RuleInstance ri : rules) {
      if (ri.getEvents().size() == 0) havenone = true;
      for (CalEvent ce : ri.getEvents()) {
	 evts.add(ce);
       }
    }

   for (int i = 0; i < evts.size(); ++i) {
      CalEvent e0 = evts.get(i);
      for (String prop : cal_props) {
	 String v0 = e0.getField(prop);
	 boolean use = false;
	 if (prop.equals("ID")) {
	    if (evts.size() == 1 || havenone) use = true;
	  }
	 for (int j = i+1; j < evts.size(); ++j) {
	    CalEvent e1 = evts.get(j);
	    String v1 = e1.getField(prop);
	    if (v1 != v0) use = true;
	  }
	 if (use) {
	    CalendarCondition cc = new CalendarCondition(prop,v0);
	    conds.add(cc);
	  }
       }
    }
}




/********************************************************************************/
/*										*/
/*	Build the decision tree 						*/
/*										*/
/********************************************************************************/

private void expandTree(TreeNode tn,Set<Condition> conds)
{
   List<RuleInstance> rules = tn.getRules();
   double tot = rules.size();
   if (tot <= 1) return;
   if (conds == null || conds.isEmpty()) return;

   Condition best = null;
   List<RuleInstance> bestp = null;
   List<RuleInstance> bestf = null;
   double bestv = 0;

   for (Condition c : conds) {
      List<RuleInstance> pr = new ArrayList<RuleInstance>();
      List<RuleInstance> fr = new ArrayList<RuleInstance>();
      for (RuleInstance ri : rules) {
	 if (c.match(ri)) pr.add(ri);
	 else fr.add(ri);
       }
      if (pr.size() == 0 || fr.size() == 0) continue;
      double vp = pr.size() / tot;
      double vf = fr.size() / tot;
      double e = - vp * Math.log(vp) - vf * Math.log(vf);
      if (e > bestv) {
	 best = c;
	 bestp = pr;
	 bestf = fr;
       }
    }

   if (best == null) return;

   tn.setCondition(best,bestp,bestf);
   expandTree(tn.getPassTree(),conds);
   expandTree(tn.getFailTree(),conds);
}



/********************************************************************************/
/*										*/
/*	Saved user rule instance						*/
/*										*/
/********************************************************************************/

private static class RuleInstance {

   private long rule_time;
   private long end_time;
   private BitSet day_set;
   private Map<String,String> world_props;
   private int	entity_state;
   private List<CalEvent> calendar_events;
   private Set<String> sensor_props;
   private UpodRule from_rule;

   RuleInstance(Element xml,UpodUniverse uu) {
      from_rule = null;
      rule_time = IvyXml.getAttrLong(xml,"WHEN");
      end_time = rule_time;
      day_set = new BitSet(dayOfWeek(rule_time));
      world_props = new HashMap<String,String>();
      entity_state = 0;
      Element we = IvyXml.getChild(xml,"WORLD");
      for (Element pe : IvyXml.children(we,"PARAM")) {
	 String id = IvyXml.getAttrString(pe,"NAME");
	 String val = IvyXml.getText(pe);
	 world_props.put(id,val);
       }
      calendar_events = new ArrayList<CalEvent>();
      for (Element ce : IvyXml.children(xml,"CALENDAR")) {
	 CalEvent cevt = new CalEvent(ce);
	 calendar_events.add(cevt);
       }
    }

   RuleInstance(UpodRule ur,UpodUniverse uu) {
      from_rule = ur;
      BasisPropertySet bps = new BasisPropertySet();
      ur.getImpliedProperties(bps);

      world_props = new HashMap<String,String>();
      entity_state = 0;

      Object t0 = bps.get("*FROMTIME");
      if (t0 != null) rule_time = ((Number) t0).longValue();
      else rule_time = 0;
      Object t1 = bps.get("*TOTIME");
      if (t1 != null) end_time = ((Number) t1).longValue();
      else end_time = rule_time;
      Object t2 = bps.get("*DAYS");
      if (t2 != null) day_set = (BitSet) t2;

      calendar_events = new ArrayList<CalEvent>();
      // need to get calendar properties as well
    }

   long getTime()			{ return rule_time; }
   long getEndTime()			{ return end_time; }
   BitSet getDays()			{ return day_set; }
   String getSensor(String id)		{ return world_props.get(id); }
   List<CalEvent> getEvents()		{ return calendar_events; }
   int getStateId()			{ return entity_state; }
   Set<String> getSensors()		{ return sensor_props; }
   UpodRule getBaseRule()		{ return from_rule; }

   List<UpodAction> getActions()	{ return null; }

}	// end of inner class RuleInstance




private static class CalEvent {

   private long start_time;
   private long end_time;
   private Map<String,String> field_values;

   CalEvent(Element ce) {
      start_time = IvyXml.getAttrLong(ce,"START");
      end_time = IvyXml.getAttrLong(ce,"END");
      field_values = new HashMap<String,String>();
      for (Element fe : IvyXml.children(ce,"FIELD")) {
	 String v = IvyXml.getAttrString(fe,"VALUE");
	 if (v != null) v = v.intern();
	 field_values.put(IvyXml.getAttrString(fe,"NAME"),v);
       }
    }

   long getStartTime()			{ return start_time; }
   long getEndTime()			{ return end_time; }
   String getField(String prop) 	{ return field_values.get(prop); }


}	// end of inner class CalEvent




/********************************************************************************/
/*										*/
/*	Potential condition							*/
/*										*/
/********************************************************************************/

private static abstract class Condition {

   protected Condition() { }

   abstract boolean match(RuleInstance ri);

   @Override public abstract boolean equals(Object o);
   @Override public abstract int hashCode();

   abstract UpodCondition getConditionTest(UpodProgram pgm);

}	// end of inner abstract class Condition


private static class TimeCondition extends Condition {

   private long start_time;
   private long end_time;
   private BitSet which_days;

   TimeCondition(long start,long end,BitSet days) {
      start_time = start;
      end_time = end;
      which_days = new BitSet();
      which_days.or(days);
    }

   @Override boolean match(RuleInstance ri) {
      long rt = dayTime(ri.getTime());
      long et = dayTime(ri.getEndTime());

      if (et < start_time) return false;
      if (end_time > 0 && rt > end_time) return false;
      if (which_days != null) {
	 if (!which_days.intersects(ri.getDays())) return false;
       }
      return true;
    }

   @Override public boolean equals(Object o) {
      if (o instanceof TimeCondition) {
	 TimeCondition tc = (TimeCondition) o;
	 if (tc.start_time == start_time && tc.end_time == end_time) return true;
       }
      return false;
    }

   @Override public int hashCode() {
      return Long.valueOf(start_time *127 ^ end_time).hashCode();
    }

   @Override UpodCondition getConditionTest(UpodProgram pgm) {
      Calendar c1 = Calendar.getInstance();
      c1.setTimeInMillis(start_time);
      Calendar c2 = Calendar.getInstance();
      c2.setTimeInMillis(end_time);
      BasisCalendarEvent evt = new BasisCalendarEvent(c1,c2);
      if (!which_days.isEmpty()) evt.setDays(which_days);
      String nm = "TIME_" + start_time + "_" + end_time;
      BasisConditionTime cc = new BasisConditionTime(pgm.getUniverse(),nm,evt);
      return cc;
    }

}	// end of inner class TimeCondition


private static class SensorCondition extends Condition {

   private String sensor_id;
   private String sensor_value;

   SensorCondition(String id,String val) {
      sensor_id = id;
      sensor_value = (val == null ? null : val.intern());
    }

   @Override boolean match(RuleInstance ri) {
      String v = ri.getSensor(sensor_id);
      if (v == null) return true;
      if (sensor_value == null) return false;
      if (!v.equals(sensor_value)) return false;
      return true;
    }

   @Override public boolean equals(Object o) {
      if (o instanceof SensorCondition) {
	 SensorCondition sc = (SensorCondition) o;
	 if (sc.sensor_id.equals(sensor_id) &&
	       sc.sensor_value == sensor_value) return true;
       }
      return false;
    }

   @Override public int hashCode() {
      int hvl = sensor_id.hashCode();
      if (sensor_value != null)
	 hvl = hvl << 3 + sensor_value.hashCode();
      return hvl;
    }

   @Override UpodCondition getConditionTest(UpodProgram pgm) {
      // UpodUniverse uu = pgm.getUniverse();
      // UpodDevice us = uu.findDevice(sensor_id);
      // UpodCondition uc = us.getCondition(sensor_value);
      // return uc;
      return null;
    }

}	// end of inner class SensorCondition


private static class CalendarCondition extends Condition {

   private String match_field;
   private String match_value;

   CalendarCondition(String fld,String val) {
      match_field = fld;
      match_value = (val == null ? null : val.intern());
    }

   @Override boolean match(RuleInstance ri) {
      List<CalEvent> evts = ri.getEvents();
      if (evts.isEmpty()) return false;
      for (CalEvent ce : evts) {
	 String v1 = ce.getField(match_field);
	 if (v1 == null && match_value == null) return true;
	 else if (v1 == null || match_value == null) continue;
	 if (v1.contains(match_value)) return true;
       }
      return true;
    }

   @Override public boolean equals(Object o) {
      if (o instanceof CalendarCondition) {
	 CalendarCondition cc = (CalendarCondition) o;
	 if (!match_field.equals(cc.match_field)) return false;
	 if (match_value == cc.match_value) return true;
       }
      return false;
    }

   @Override public int hashCode() {
      int hc = match_field.hashCode();
      if (match_value != null) hc = hc*7 ^ match_value.hashCode();
      return hc;
    }

   @Override UpodCondition getConditionTest(UpodProgram pgm) {
      String nm = "CAL_" + match_field + "_" +  match_value;
      BasisConditionCalendarEvent gcond = new BasisConditionCalendarEvent(pgm,nm);
      gcond.addFieldMatch(match_field,match_value);
      return gcond;
    }

}	// end of inner class CalendarCondition




/********************************************************************************/
/*										*/
/*	Node of the resultant decision tree					*/
/*										*/
/********************************************************************************/

private class TreeNode {

   private Condition	test_condition;
   private List<RuleInstance> rule_set;
   private TreeNode	pass_tree;
   private TreeNode	fail_tree;
   private TreeNode	parent_node;

   TreeNode(Collection<RuleInstance> rules,TreeNode par) {
      test_condition = null;
      parent_node = par;
      rule_set = new ArrayList<RuleInstance>(rules);
      pass_tree = null;
      fail_tree = null;
    }

   void setCondition(Condition c,Collection<RuleInstance> pass,Collection<RuleInstance> fail) {
      test_condition = c;
      pass_tree = new TreeNode(pass,this);
      fail_tree = new TreeNode(fail,this);
    }

   List<RuleInstance> getRules()		{ return rule_set; }
   TreeNode getPassTree()			{ return pass_tree; }
   TreeNode getFailTree()			{ return fail_tree; }
   Condition getCondition()			{ return test_condition; }
   TreeNode getParent() 			{ return parent_node; }
   boolean isLeaf()				{ return pass_tree == null; }

}	// end of inner class TreeNode




/********************************************************************************/
/*										*/
/*	Generate result 							*/
/*										*/
/********************************************************************************/

private void generateRules(TreeNode tn)
{
   if (!tn.isLeaf()) {
      generateRules(tn.getPassTree());
      generateRules(tn.getFailTree());
      return;
    }
   RuleInstance ri = tn.getRules().get(0);
   if (ri.getBaseRule() != null) return;	// rule already in set

   Condition c = tn.getCondition();
   List<UpodCondition> cset = new ArrayList<UpodCondition>();
   if (c != null) {
      UpodCondition cond = c.getConditionTest(for_program);
      if (cond != null) cset.add(cond);
    }
   for (TreeNode pn = tn.getParent(); pn != null; pn = pn.getParent()) {
      Condition pc = pn.getCondition();
      if (pc == null) continue;
      UpodCondition pcond = pc.getConditionTest(for_program);
      if (pcond != null) cset.add(pcond);
    }

   UpodCondition rcond = null;
   if (cset.isEmpty()) return;
   if (cset.size() == 1) rcond = cset.get(0);
   else {
      UpodCondition [] conds = new UpodCondition[cset.size()];
      conds = cset.toArray(conds);
      rcond = new BasisConditionLogical.And(conds);
    }
   List<UpodAction> racts = ri.getActions();
   if (rcond == null || racts == null) return;

   BasisRule rule = new BasisRule(rcond,racts,null,100);
   for_program.addRule(rule);
}


}	// end of class BasisRuleDeducer




/* end of BasisRuleDeducer.java */

