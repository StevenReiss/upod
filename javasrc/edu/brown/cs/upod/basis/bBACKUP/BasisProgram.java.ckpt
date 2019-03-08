/********************************************************************************/
/*										*/
/*		BasisProgram.java						*/
/*										*/
/*	Basic implementation of a program					*/
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
import java.util.concurrent.*;
import java.lang.reflect.*;


public class BasisProgram implements UpodProgram, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SortedSet<UpodRule>	rule_list;
private UpodUniverse		for_universe;
private Set<UpodCondition>	active_conditions;
private RuleConditionHandler	cond_handler;
private Map<UpodWorld,Updater>	active_updates;
private Map<String,UpodWorld>	known_worlds;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisProgram(UpodUniverse uu)
{
   for_universe = uu;
   rule_list = new ConcurrentSkipListSet<UpodRule>(new RuleComparator());
   active_conditions = new HashSet<UpodCondition>();
   active_updates = new HashMap<UpodWorld,Updater>();
   cond_handler = new RuleConditionHandler();
   known_worlds = new HashMap<String,UpodWorld>();
   UpodWorld cw = BasisFactory.getFactory().getCurrentWorld(uu);
   known_worlds.put(cw.getUID(),cw);
}


public BasisProgram(UpodUniverse uu,Element xml)
{
   this(uu);

   if (xml != null) {
      for (Element re : IvyXml.children(xml,"RULE")) {
	 UpodRule br = createRule(re);
	 if (br != null) rule_list.add(br);
       }
    }

   updateConditions();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public List<UpodRule> getRules()
{
   return new ArrayList<UpodRule>(rule_list);
}


@Override public UpodRule findRule(String id)
{
   if (id == null) return null;
   
   for (UpodRule ur : rule_list) {
      if (ur.getUID().equals(id) || ur.getName().equals(id)) 
         return ur;
    }
   return null;
}


@Override public synchronized void addRule(UpodRule ur)
{
   rule_list.add(ur);
   updateConditions();
}


@Override public synchronized void removeRule(UpodRule ur)
{
   rule_list.remove(ur);
   updateConditions();
}


@Override public UpodUniverse getUniverse()
{
   return for_universe;
}


@Override public UpodWorld createWorld(UpodWorld base)
{
   if (base == null) base = getWorld(null);

   UpodWorld nw = base.createClone();
   known_worlds.put(nw.getUID(),nw);
   return nw;
}


@Override public UpodWorld getWorld(String uid)
{
   if (uid == null) {
      return BasisFactory.getFactory().getCurrentWorld(for_universe);
    }
   return known_worlds.get(uid);
}


@Override public boolean removeWorld(UpodWorld w)
{
    if (w == null || w.isCurrent()) return false;
    if (known_worlds.remove(w.getUID()) == null) return false;
    return true;
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public void outputXml(IvyXmlWriter xw)
{
   xw.begin("PROGRAM");
   for (UpodRule ur : rule_list) ur.outputXml(xw);
   xw.end("PROGRAM");
}




/********************************************************************************/
/*										*/
/*	Maintain active conditions						*/
/*										*/
/********************************************************************************/

private void updateConditions()
{
   Set<UpodCondition> del = new HashSet<UpodCondition>(active_conditions);

   for (UpodRule ur : rule_list) {
      UpodCondition uc = ur.getCondition();
      del.remove(uc);
      if (!active_conditions.contains(uc)) {
	 active_conditions.add(uc);
	 uc.addConditionHandler(cond_handler);
       }
    }

   for (UpodCondition uc : del) {
      uc.removeConditionHandler(cond_handler);
      active_conditions.remove(uc);
    }
}



private void conditionChange(UpodWorld w)
{
   conditionChange(w,null,null);
}


private void conditionChange(UpodWorld w,UpodCondition c,UpodPropertySet ps)
{
   w.updateLock();
   try {
      if (c != null) w.addTrigger(c,ps);
      Updater upd = active_updates.get(w);
      if (upd != null) {
	 upd.runAgain();
       }
      else {
	 upd = new Updater(w);
	 active_updates.put(w,upd);
	 BasisThreadPool.start(upd);
       }
    }
   finally {
      w.updateUnlock();
    }
}



private class Updater implements Runnable {

   private UpodWorld for_world;
   private boolean run_again;

   Updater(UpodWorld w) {
      for_world = w;
      run_again = true;
    }

   void runAgain() {
      for_world.updateLock();
      try {
	 run_again = true;
       }
      finally {
	 for_world.updateUnlock();
       }
    }

   @Override public void run() {
      for_world.updateLock();
      try {
         run_again = false;
       }
      finally {
         for_world.updateUnlock();
       }
      for ( ; ; ) {
         UpodTriggerContext ctx = null;
         for_world.updateLock();
         try {
            ctx = for_world.waitForUpdate();
            run_again = false;
          }
         finally {
            for_world.updateUnlock();
          }
   
         runOnce(for_world,ctx);
   
         for_world.updateLock();
         try {
            if (!run_again) {
               active_updates.remove(for_world);
               resetTriggers();
               break;
             }
          }
         finally {
            for_world.updateUnlock();
          }
       }
   
    }

}	// end of inner class Updater




private class RuleConditionHandler implements UpodConditionHandler {

   @Override public void conditionOn(UpodWorld w,UpodCondition c,
	 UpodPropertySet p) {
      conditionChange(w);
    }

   @Override public void conditionOff(UpodWorld w,UpodCondition c) {
      conditionChange(w);
    }

   @Override public void conditionError(UpodWorld w,UpodCondition c,
	 Throwable cause) {
    }

   @Override public void conditionTrigger(UpodWorld w,UpodCondition c,
	 UpodPropertySet p) {
      if (p == null) p = new BasisPropertySet();
      conditionChange(w,c,p);
    }

}	// end of inner class RuleConditionHandler




/********************************************************************************/
/*										*/
/*	Program run methods							*/
/*										*/
/********************************************************************************/

@Override public synchronized boolean runOnce(UpodWorld w,UpodTriggerContext ctx)
{
   boolean rslt = false;
   Set<UpodDevice> entities = new HashSet<UpodDevice>();
   if (w == null) w = BasisFactory.getFactory().getCurrentWorld(for_universe);

   Collection<UpodRule> rules = new ArrayList<UpodRule>(rule_list);

   BasisLogger.logI("CHECK RULES at " + new Date());

   for (UpodRule r : rules) {
      Set<UpodDevice> rents = r.getDevices();
      if (containsAny(entities,rents)) continue;
      try {
	 if (startRule(r,w,ctx)) {
	    rslt = true;
	    entities.addAll(rents);
	  }
       }
      catch (UpodException e) {
	 BasisLogger.logE("Problem switch rule " + r.getName(),e);
       }
    }

   return rslt;
}



private boolean startRule(UpodRule r,UpodWorld w,UpodTriggerContext ctx)
{
   return r.apply(w,ctx);
}


private void resetTriggers()
{
   // reset any triggers after rules run completely
}





private boolean containsAny(Set<UpodDevice> s1,Set<UpodDevice> s2)
{
   for (UpodDevice u2 : s2) {
      if (s1.contains(u2)) return true;
    }
   return false;
}




/********************************************************************************/
/*										*/
/*	Rule priority comparator						*/
/*										*/
/********************************************************************************/

private static class RuleComparator implements Comparator<UpodRule> {

   @Override public int compare(UpodRule r1,UpodRule r2) {
      double v = r1.getPriority() - r2.getPriority();
      if (v > 0) return -1;
      if (v < 0) return 1;
      if (r1.getPriority() >= 100) {
         long t1 = r1.getCreationTime() - r2.getCreationTime();
         if (t1 > 0) return -1;
         if (t1 < 0) return 1;
       }
      int v1 = r1.getName().compareTo(r2.getName());
      if (v1 != 0) return v1;
      return r1.getUID().compareTo(r2.getUID());
    }

}	// end of inner class RuleComparator



/********************************************************************************/
/*										*/
/*	Input methods								*/
/*										*/
/********************************************************************************/

@Override public UpodRule createRule(Element xml)
{
   String id = IvyXml.getAttrString(xml,"ID");
   if (id != null) {
      for (UpodRule ur : getRules()) {
	 if (ur.getUID().equals(id)) return ur;
       }
    }

   if (id == null || IvyXml.getAttrString(xml,"CLASS") == null) {
      return new BasisRule(this,xml);
    }

   return (UpodRule) loadXmlElement(xml);
}





@Override public UpodCondition createCondition(Element xml)
{
   if (xml == null) return null;

   String nm = IvyXml.getTextElement(xml,"NAME");
   String typ = IvyXml.getAttrString(xml,"TYPE");

   if (typ == null || typ.equals("PRIM")) {
      UpodCondition uc = for_universe.findBasicCondition(nm);
      if (uc != null) return uc;
    }

   if (IvyXml.getAttrString(xml,"CLASS") == null) {
      switch (typ) {
	 case "TIME" :
	    return new BasisConditionTime(this,xml);
	 case "AND" :
	    return new BasisConditionLogical.And(this,xml);
         case "OR" :
            return new BasisConditionLogical.Or(this,xml);
	 case "TIMED" :
	 case "TRIGGER" :
	    return new BasisConditionDuration(this,xml);
	 case "GOOGLE" :
	    return new BasisConditionCalendarEvent(this,xml);
	 case "RANGE" :
	    return new BasisConditionRange(this,xml);
	 case "TIMETRIGGER" :
	    return new BasisConditionTriggerTime(this,xml);
	 default :
	    BasisLogger.logE("Unknown condition: " +
		  IvyXml.convertXmlToString(xml));
	    return null;
       }
    }
   // non-primitive condition
   return (UpodCondition) loadXmlElement(xml);
}


@Override public UpodAction createAction(Element xml)
{
   if (IvyXml.getAttrString(xml,"CLASS") == null) {
      return new BasisAction(this,xml);
    }

   return (UpodAction) loadXmlElement(xml);
}


@Override public UpodDevice createDevice(Element xml)
{
   String uid = IvyXml.getAttrString(xml,"ID");
   for (UpodDevice ue : for_universe.getDevices()) {
      if (ue.getUID().equals(uid)) return ue;
    }

   String nm = IvyXml.getAttrString(xml,"NAME");
   for (UpodDevice ue : for_universe.getDevices()) {
      if (ue.getName().equals(nm)) return ue;
    }

   return null;
}


@Override public UpodDevice findDevice(String id)
{
   if (id == null) return null;
   
   for (UpodDevice ue : for_universe.getDevices()) {
      if (ue.getUID().equals(id)) return ue;
    }
   
   for (UpodDevice ue : for_universe.getDevices()) {
      if (ue.getName().equals(id)) return ue;
    }
   
   return null;
}


@Override public UpodTransition createTransition(UpodDevice ue,Element xml)
{
   if (xml == null) return null;

   String nm = IvyXml.getTextElement(xml,"NAME");
   for (UpodTransition ut : ue.getTransitions()) {
      if (ut.getName().equals(nm)) return ut;
    }
   return null;
}




@Override public UpodParameterSet createParameterSet(Element xml)
{
   return new BasisParameterSet(xml,null);
}


@Override public UpodPropertySet createPropertySet(Element xml)
{
   return new BasisPropertySet(xml);
}




private Object loadXmlElement(Element xml)
{
   if (xml == null) return null;

   String cnm = IvyXml.getAttrString(xml,"CLASS");
   if (cnm == null) return null;

   try {
      Class<?> c = Class.forName(cnm);
      Constructor<?> cnst = c.getConstructor(UpodProgram.class,Element.class);
      return cnst.newInstance(this,xml);
    }
   catch (Throwable t) {
      BasisLogger.logE("Problem creating " + cnm,t);
      return null;
    }
}


}	// end of class BasisProgram




/* end of BasisProgram.java */

