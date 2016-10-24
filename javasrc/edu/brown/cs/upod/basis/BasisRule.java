/********************************************************************************/
/*                                                                              */
/*              BasisRule.java                                                  */
/*                                                                              */
/*      Basic implementation of a rule                                          */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.upod.basis;

import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.xml.*;
import org.w3c.dom.*;

import java.util.*;

public class BasisRule implements UpodRule, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          rule_name;
private UpodCondition   for_condition;
private List<UpodAction>  for_actions;
private List<UpodAction> exception_actions;
private double          rule_priority;
private String          unique_id;
private volatile RuleRunner      active_rule;
private String          rule_description;
private String          rule_label;
private long            creation_time;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisRule(UpodCondition c,Collection<UpodAction> a,
      Collection<UpodAction> ea,double priority)
        throws UpodConditionException
{
   for_condition = c;
   for_actions = new ArrayList<UpodAction>();
   if (a != null) for_actions.addAll(a);
   exception_actions = null;
   rule_description = null;
   rule_label = null;
   if (ea != null) exception_actions = new ArrayList<UpodAction>(ea);
   rule_priority = priority;
   unique_id = BasisWorld.getNewUID();
   creation_time = System.currentTimeMillis();
}



public BasisRule(UpodProgram bp,Element xml) 
{
   rule_name = IvyXml.getAttrString(xml,"NAME");
   if (rule_name != null && rule_name.equals("")) rule_name = null;
   rule_priority = IvyXml.getAttrDouble(xml,"PRIORITY");
   Element ce = IvyXml.getChild(xml,"CONDITION");
   for_condition = bp.createCondition(ce);
   unique_id = IvyXml.getAttrString(xml,"ID");
   if (unique_id == null) unique_id = BasisWorld.getNewUID();  
   for_actions = new ArrayList<UpodAction>();
   Element acts = IvyXml.getChild(xml,"ACTIONS");
   for (Element ae : IvyXml.children(acts,"ACTION")) {
      UpodAction ba = bp.createAction(ae);
      for_actions.add(ba);
    }
   exception_actions = null;
   Element eacts = IvyXml.getChild(xml,"EXCEPTION");
   for (Element ae : IvyXml.children(eacts,"ACTION")) {
      if (exception_actions == null) 
         exception_actions = new ArrayList<UpodAction>();
      UpodAction ba = bp.createAction(ae);
      exception_actions.add(ba);
    }
   String desc = IvyXml.getTextElement(xml,"DESCRIPTION");
   if (desc != null) desc = Coder.unescape(desc);
   if (desc != null && !desc.equals(getName())) rule_description = desc;
   else rule_description = null;
   
   String lbl = IvyXml.getTextElement(xml,"LABEL");
   if (lbl != null) lbl = Coder.unescape(lbl);
   if (lbl != null && !lbl.equals(getLabel())) rule_label = lbl;
   else rule_label = null;
   
   creation_time = IvyXml.getAttrLong(xml,"CREATED",System.currentTimeMillis());
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName() 
{  
   if (rule_name == null) {
      rule_name = for_condition.getName() + "=>";
      if (for_actions.size() == 0) rule_name += "<NIL>";
      else {
         UpodAction a = for_actions.get(0);
         rule_name += a.getName();
         if (for_actions.size() > 1) rule_name += "...";
       }
    }
   
   return rule_name;
}

@Override public String getDescription()     
{ 
   if (rule_description != null) return rule_description;
   return getName();
}

@Override public void setDescription(String d) 
{
   rule_description = d;
}

@Override public String getLabel()
{
   if (rule_label != null) return rule_label;
   
   StringBuffer buf = new StringBuffer();
   buf.append("WHEN ");
   buf.append(for_condition.getLabel());
   buf.append(" DO ");
   int ctr = 0;
   for (UpodAction a : for_actions) {
      if (ctr++ > 0) buf.append(", ");
      if (a.getLabel() == null) buf.append("?");
      else buf.append(a.getLabel());
    }
   return buf.toString();
}

@Override public void setLabel(String s) 
{
   rule_label = s;
}


@Override public String getUID()                        { return unique_id; }

@Override public UpodCondition getCondition()           { return for_condition; }

@Override public List<UpodAction> getActions()          { return for_actions; }

@Override public List<UpodAction> getExceptionActions()
{ 
   return exception_actions;
}

@Override public double getPriority()                   { return rule_priority; }

@Override public void setPriority(double p)             { rule_priority = p; }


@Override public long getCreationTime()                 { return creation_time; }

@Override public boolean isExplicit()                   { return true; }

@Override public Set<UpodDevice> getDevices() 
{
   Set<UpodDevice> rslt = new HashSet<UpodDevice>();
   
   for (UpodAction ua : for_actions) {
      rslt.add(ua.getDevice());
    }
   
   return rslt;
}


@Override public Set<UpodDevice> getSensors()
{
   Set<UpodDevice> rslt = new HashSet<UpodDevice>();
   
   for_condition.getSensors(rslt);
   
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Methods for handling deduction                                          */
/*                                                                              */
/********************************************************************************/

@Override public UpodPropertySet getImpliedProperties(UpodPropertySet ups)
{
   if (ups == null) ups = new BasisPropertySet();
   
   for_condition.addImpliedProperties(ups);
   for (UpodAction ua : for_actions) {
      ua.addImpliedProperties(ups);
    }
   
   return ups;
}



/********************************************************************************/
/*                                                                              */
/*      Application methods                                                     */
/*                                                                              */
/********************************************************************************/

@Override public boolean apply(UpodWorld w,UpodTriggerContext ctx) 
        throws UpodConditionException, UpodActionException
{
   UpodPropertySet ps = null;
   if (for_condition != null) {
      if (ctx != null) ps = ctx.checkCondition(for_condition);
      if (ps == null) ps = for_condition.getCurrentStatus(w);
      if (ps == null) return false;
    }
   
   BasisLogger.logI("Apply " + getLabel());
   
   if (for_actions != null) {
      active_rule = new RuleRunner(w,ps);
      active_rule.applyRule();
    }
   
   return true;
}


@Override public void abort()
{
   RuleRunner rr = active_rule;
   if (rr != null) rr.abort();
}




private class RuleRunner implements Runnable {
   
   private UpodWorld for_world;
   private UpodPropertySet param_set;
   private Thread runner_thread;
   private boolean is_aborted;
   private Throwable fail_code;
   
   RuleRunner(UpodWorld w,UpodPropertySet ps) {
      for_world = w;
      param_set = ps;
      fail_code = null;
      is_aborted = false;
      runner_thread = null;
    }
   
   void abort() {
      synchronized (this) {
         // don't want to interrupt thread if it has finished rule
         if (active_rule != null && runner_thread != null) {
            is_aborted = true;
            if (runner_thread != Thread.currentThread()) 
               runner_thread.interrupt();
          }
       }
    }

   @Override public void run() {
      runner_thread = Thread.currentThread();
      applyRule();
    }
   
   void applyRule() {
      try {
         try {
            for (UpodAction a : for_actions) {
               a.perform(for_world,param_set);
               synchronized (this) {
                  if (Thread.currentThread().isInterrupted() || is_aborted) {
                     break;
                   }
                }
             }
          }
         catch (UpodActionException ex) {
            fail_code = ex;
          }
         catch (Throwable t) {
            BasisLogger.log("Problem execution action: " + t);
            t.printStackTrace();
            fail_code = t;
          }
         if (fail_code != null && exception_actions != null) {
            try {
               for (UpodAction a : exception_actions) {
                  a.perform(for_world,param_set);
                  synchronized (this) {
                     if (Thread.currentThread().isInterrupted() || is_aborted) {
                        break;
                      } 
                   }
                }
             }
            catch (Throwable t) { 
               BasisLogger.log("Problem handling exception action: " + t);
               t.printStackTrace();
             }
          }
       }
      finally {
         synchronized (this) {
            active_rule = null;
            runner_thread = null;
          }
       }
    }
}

        


/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw) 
{
   xw.begin("RULE");
   xw.field("CLASS",this.getClass().getName());
   xw.field("ID",getUID());
   xw.field("NAME",getName());
   xw.field("PRIORITY",getPriority());
   xw.field("EXPLICIT",isExplicit());
   xw.field("CREATED",creation_time);
   xw.textElement("LABEL",Coder.escape(getLabel()));
   xw.textElement("DESC",Coder.escape(getDescription()));
   
   if (for_condition != null) for_condition.outputXml(xw);
   if (for_actions != null) {
      xw.begin("ACTIONS");
      for (UpodAction a : for_actions) a.outputXml(xw);
      xw.end("ACTIONS");
    }
   if (exception_actions != null) {
      xw.begin("EXCEPTION");
      for (UpodAction a : for_actions) a.outputXml(xw);
      xw.end("EXCEPTION");
    }
   xw.end("RULE");
}

}       // end of class BasisRule




/* end of BasisRule.java */

