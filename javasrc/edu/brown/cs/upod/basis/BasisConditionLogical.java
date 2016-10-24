/********************************************************************************/
/*										*/
/*		BasisConditionLogical.java					*/
/*										*/
/*	description of class							*/
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



public abstract class BasisConditionLogical extends BasisCondition
	implements UpodCondition, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/


protected List<BasisCondition>	arg_conditions;
private CondUpdater		cond_updater;
private boolean 		first_time;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisConditionLogical(UpodCondition ... cond) throws UpodConditionException
{
   super(cond[0].getUniverse());

   arg_conditions = new ArrayList<BasisCondition>();
   boolean havetrigger = false;
   
   for (UpodCondition c : cond) {
      if (c.isTrigger()) {
         if (havetrigger)
            throw new UpodConditionException("Trigger must be first condition");
         havetrigger = true;
       }
      arg_conditions.add((BasisCondition) c);
    }

   first_time = true;

   setupTriggers();
}

protected BasisConditionLogical(UpodProgram pgm,Element xml)
{
   super(pgm,xml);

   arg_conditions = new ArrayList<BasisCondition>();
   for (Element ce : IvyXml.children(xml,"CONDITION")) {
      arg_conditions.add((BasisCondition) pgm.createCondition(ce));
    }

   first_time = true;

   setupTriggers();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public void getSensors(Collection<UpodDevice> rslt)
{
   for (BasisCondition bc : arg_conditions) {
      bc.getSensors(rslt);
    }
}



@Override public boolean isTrigger()
{
   for (BasisCondition bc : arg_conditions) {
      if (bc.isTrigger()) return true;
    }
   
   return false;
}



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

@Override public void setTime(UpodWorld w)
{
   if (first_time) {
     first_time = false;
     checkState(w,null);
    }

   cond_updater.beginHold(w);
   for (UpodCondition c : arg_conditions) {
      c.setTime(w);
    }
   cond_updater.endHold(w);
}



private void setupTriggers()
{
   cond_updater = new CondUpdater();
   for (UpodCondition c : arg_conditions) {
      c.addConditionHandler(cond_updater);
    }
}



private void checkState(UpodWorld w,BasisTriggerContext ctx)
{
   try {
      UpodPropertySet ps = recompute(w,ctx);
      BasisLogger.logI("CONDITION " + getLabel() + " " + (ps != null));
      if (ps != null) {
	 if (ps.get("*TRIGGER*") != null) fireTrigger(w,ps);
	 else fireOn(w,ps);
       }
      else fireOff(w);
    }
   catch (Throwable t) {
      fireError(w,t);
    }
}


abstract protected UpodPropertySet recompute(UpodWorld w,BasisTriggerContext ctx)
	throws UpodConditionException;


private class CondUpdater implements UpodConditionHandler {

   private Set<UpodWorld> hold_worlds;
   private Map<UpodWorld,BasisTriggerContext> change_worlds;

   CondUpdater() {
      hold_worlds = new HashSet<UpodWorld>();
      change_worlds = new HashMap<UpodWorld,BasisTriggerContext>();
    }

   synchronized void beginHold(UpodWorld w) {
      hold_worlds.add(w);
      change_worlds.remove(w);
    }

   void endHold(UpodWorld w) {
      BasisTriggerContext ctx = null;
      synchronized (this) {
	 hold_worlds.remove(w);
	 ctx = change_worlds.remove(w);
	 if (ctx == null) return;
       }
      checkState(w,ctx);
    }

   void update(UpodWorld w,BasisTriggerContext ctx) {
      synchronized (this) {
	if (hold_worlds.contains(w)) {
	   BasisTriggerContext octx = change_worlds.get(w);
	   if (octx != null && ctx != null) octx.addContext(ctx);
	   else if (octx == null) {
	      if (ctx == null) ctx = new BasisTriggerContext();
	      change_worlds.put(w,ctx);
	    }
	   return;
	 }
       }
      checkState(w,ctx);
    }

   @Override public void conditionError(UpodWorld w,UpodCondition c,Throwable t) {
      update(w,null);
    }

   @Override public void conditionOff(UpodWorld w,UpodCondition c) {
      update(w,null);
    }

   @Override public void conditionOn(UpodWorld w,UpodCondition c,
	 UpodPropertySet ps) {
      update(w,null);
    }

   @Override public void conditionTrigger(UpodWorld w,UpodCondition c,
         UpodPropertySet ps) {
      BasisTriggerContext ctx = new BasisTriggerContext(c,ps);
      update(w,ctx);
    }

}	// end of inner class CondUpdater




/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   outputHeader(xw);
   outputLocalXml(xw);
   for (UpodCondition uc : arg_conditions) {
      uc.outputXml(xw);
    }
   outputTrailer(xw);
}


protected void outputLocalXml(IvyXmlWriter xw)		{ }



/********************************************************************************/
/*										*/
/*	AND implementation							*/
/*										*/
/********************************************************************************/

static public class And extends BasisConditionLogical {

   And(UpodCondition ... cond) throws UpodConditionException {
      super(cond);
      int tct = 0;
      for (BasisCondition bc : arg_conditions) {
         if (bc.isTrigger()) ++tct;
       }
      if (tct > 1) throw new UpodConditionException("Can't AND multiple triggers");
    }

   public And(UpodProgram pgm,Element xml) {
      super(pgm,xml);
    }

   @Override public String getName() {
      StringBuffer buf = new StringBuffer();
      for (UpodCondition c : arg_conditions) {
	 if (buf.length() > 0) buf.append("&&");
	 buf.append(c.getName());
       }
      return buf.toString();
    }

   @Override public String getLabel() {
      StringBuffer buf = new StringBuffer();
      for (UpodCondition c : arg_conditions) {
	 if (buf.length() > 0) buf.append(" AND ");
	 buf.append(c.getLabel());
       }
      return buf.toString();
   }

   @Override public String getDescription() {
      StringBuffer buf = new StringBuffer();
      for (UpodCondition c : arg_conditions) {
	 if (buf.length() > 0) buf.append("&&");
	 buf.append(c.getDescription());
       }
      return buf.toString();
    }

   @Override protected	UpodPropertySet recompute(UpodWorld world,BasisTriggerContext ctx)
	   throws UpodConditionException {
      UpodPropertySet ups = new BasisPropertySet();
      for (UpodCondition c : arg_conditions) {
	 UpodPropertySet ns = null;
	 if (ctx != null) ns = ctx.checkCondition(c);
	 if (ns == null) ns = c.getCurrentStatus(world);
	 if (ns == null) return null;
	 ups.putAll(ns);
       }
      return ups;
    }

   @Override protected boolean checkOverlapConditions(BasisCondition bc) {
      for (BasisCondition ac : arg_conditions) {
	 if (!ac.checkOverlapConditions(bc)) return false;
       }
      return true;
    }

   @Override public void addImpliedProperties(UpodPropertySet ups)
   {
      for (BasisCondition ac : arg_conditions) {
	 ac.addImpliedProperties(ups);
       }
   }

   @Override protected boolean isConsistentWith(BasisCondition bc) {
      for (BasisCondition ac : arg_conditions) {
	 if (!ac.isConsistentWith(bc)) return false;
       }
      return true;
    }

   @Override protected void outputLocalXml(IvyXmlWriter xw) {
      xw.field("TYPE","AND");
    }

}	// end of inner class And




/********************************************************************************/
/*										*/
/*	OR implementation							*/
/*										*/
/********************************************************************************/

static public class Or extends BasisConditionLogical {

   Or(UpodCondition ... cond) throws UpodConditionException {
      super(cond);
      int tct = 0;
      for (BasisCondition bc : arg_conditions) {
         if (bc.isTrigger()) ++tct;
       }
      if (tct != 0 && tct != arg_conditions.size())
         throw new UpodConditionException("OR must be either all triggers or no triggers");
    }
   
   public Or(UpodProgram pgm,Element xml) {
      super(pgm,xml);
    }
   
   @Override public String getName() {
      StringBuffer buf = new StringBuffer();
      for (UpodCondition c : arg_conditions) {
         if (buf.length() > 0) buf.append("||");
         buf.append(c.getName());
       }
      return buf.toString();
    }
   
   @Override public String getLabel() {
      StringBuffer buf = new StringBuffer();
      for (UpodCondition c : arg_conditions) {
         if (buf.length() > 0) buf.append(" OR ");
         buf.append(c.getLabel());
       }
      return buf.toString();
    }
   
   @Override public String getDescription() {
      StringBuffer buf = new StringBuffer();
      for (UpodCondition c : arg_conditions) {
         if (buf.length() > 0) buf.append("||");
         buf.append(c.getDescription());
       }
      return buf.toString();
    }
   
   @Override protected	UpodPropertySet recompute(UpodWorld world,BasisTriggerContext ctx)
   throws UpodConditionException {
      UpodPropertySet ups = null;
      for (UpodCondition c : arg_conditions) {
         UpodPropertySet ns = null;
         if (ctx != null) ns = ctx.checkCondition(c);
         if (ns == null) ns = c.getCurrentStatus(world);
         if (ns != null) {
            if (ups == null) ups = new BasisPropertySet();
            ups.putAll(ns);
          }
       }
      return ups;
    }
   
   @Override protected boolean checkOverlapConditions(BasisCondition bc) {
      for (BasisCondition ac : arg_conditions) {
         if (!ac.checkOverlapConditions(bc)) return false;
       }
      return true;
    }
   
   @Override public void addImpliedProperties(UpodPropertySet ups)
   {
      for (BasisCondition ac : arg_conditions) {
         ac.addImpliedProperties(ups);
       }
    }
   
   @Override protected boolean isConsistentWith(BasisCondition bc) {
      for (BasisCondition ac : arg_conditions) {
         if (ac.isConsistentWith(bc)) return true;
       }
      return true;
    }
   
   @Override protected void outputLocalXml(IvyXmlWriter xw) {
      xw.field("TYPE","OR");
    }
   
}	// end of inner class Or




}	// end of class BasisConditionLogical




/* end of BasisConditionLogical.java */

