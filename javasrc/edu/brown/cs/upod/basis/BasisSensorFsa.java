/********************************************************************************/
/*										*/
/*		BasisSensorFsa.java						*/
/*										*/
/*	Sensor implementing an FSA						*/
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.upod.UpodCondition;
import edu.brown.cs.upod.upod.UpodParameter;
import edu.brown.cs.upod.upod.UpodPropertySet;
import edu.brown.cs.upod.upod.UpodUniverse;
import edu.brown.cs.upod.upod.UpodConditionHandler;
import edu.brown.cs.upod.upod.UpodWorld;



public class BasisSensorFsa extends BasisDevice
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		sensor_label;
private String		sensor_name;
private Set<String>	known_states;
private boolean 	is_finished;
private List<Transition> all_transitions;
private Map<UpodWorld,List<Instance>> active_instances;
private String          start_name;
private TimeCheck       next_check;
private Map<String,UpodParameter> parameter_map;

private static final String ANY_STATE = "*";
private static final String START_STATE = "<START>";


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisSensorFsa(UpodUniverse uu,String name)
{
   super(uu);

   sensor_label = name;
   sensor_name = uu.getName() + NSEP + name.replace(" ",WSEP);

   initialize();
}


public BasisSensorFsa(UpodUniverse uu,Element xml)
{
   super(uu,xml);

   sensor_label = IvyXml.getAttrString(xml,"LABEL");
   sensor_name = IvyXml.getAttrString(xml,"NAME");
   initialize();

   start_name = IvyXml.getAttrString(xml,"START");
 
   for (Element telt : IvyXml.children(xml,"TRANSITION")) {
      Transition t = new Transition(telt);
      all_transitions.add(t);
    }
}



private void initialize()
{
   is_finished = false;
   all_transitions = new ArrayList<Transition>();
   active_instances = new HashMap<UpodWorld,List<Instance>>();
   parameter_map = new HashMap<String,UpodParameter>();
   known_states = new HashSet<String>();
   start_name = START_STATE;
   next_check = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()		{ return sensor_name; }

@Override public String getLabel()		{ return sensor_label; }

@Override public String getDescription()
{
   return getLabel();
}



/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

public void addTransition(String from,UpodCondition when,String to)
{
   from = findState(from);
   to = findState(to);
   Transition t = new Transition(from,when,to);
   all_transitions.add(t);
}


public void addTimeout(String from,long time,String to)
{
   from = findState(from);
   to = findState(to);
   Transition t = new Transition(from,time,to);
   all_transitions.add(t);
}


public void finish()
{
   is_finished = true;
   
   for (Transition t : all_transitions) {
      UpodCondition c = t.getCondition();
      if (c == null) continue;
      c.addConditionHandler(new ConditionCheck(t));
    }
   
   for (String s : known_states) {
      if (s == null) continue; 
      if (s.equals(START_STATE) || s.equals(ANY_STATE)) continue;
      UpodParameter p = BasisParameter.createBooleanParameter(s);
      p = addParameter(p);
      parameter_map.put(s,p);
      addConditions(p);
    }
}


private String findState(String name)
{
   if (name == null || name.equals("")) name = start_name;
   if (name == null) name = START_STATE;
   if (name.equals(ANY_STATE)) return name;
   if (name.equals("*")) return ANY_STATE;
   
   name = name.toUpperCase();
   if (known_states.contains(name)) return name;
   if (is_finished) return null;
   
   known_states.add(name);
   
   return name;
}




/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

private synchronized void applyTransition(UpodWorld w,Transition t)
{
   List<Instance> instlist = active_instances.get(w);
   if (instlist != null) {
      for (Instance inst : instlist) {
         if (inst.applyTransition(t)) {
            if (!inst.isActive()) instlist.remove(inst);
            return;
          }
       }
    }
   
   if (t.getFromState().equals(start_name) && t.getCondition() != null) {
      Instance inst = new Instance();
      if (instlist == null) {
         instlist = new ArrayList<Instance>();
         active_instances.put(w,instlist);
       }
      instlist.add(inst);
      inst.applyTransition(t);
    }
   
   updateConditions(w);
}


private synchronized void updateTime(UpodWorld w)
{
   long when = w.getTime();
   List<Instance> instlist = active_instances.get(w);
   if (instlist == null) return;
   for (Instance inst : instlist) {
      inst.handleTime(when);
      if (!inst.isActive()) instlist.remove(inst);
    }
   
   if (w.isCurrent()) {
      if (next_check != null) next_check.cancel();
      long next = 0;
      for (Instance inst : instlist) {
         for (Transition t : all_transitions) {
            if (t.getCondition() == null && t.getDelayTime() > 0) {
               if (inst.inState(t.getFromState())) {
                  long ttime = inst.getEnterTime() + t.getDelayTime();
                  if (next == 0 || ttime < next) next = ttime;
                }
             }
          }
       }
      if (next != 0) {
         Timer t = getTimer();
         next_check = new TimeCheck();
         t.schedule(next_check,next);
       }
    }
   
   updateConditions(w);
}




private void updateConditions(UpodWorld w) 
{
   Set<String> active = new HashSet<String>();
   List<Instance> instlist = active_instances.get(w);
   if (instlist != null) {
      for (Instance inst : instlist) {
        active.add(inst.getCurrentState());
       }
    }
   
   for (Map.Entry<String,UpodParameter> ent : parameter_map.entrySet()) {
      String st = ent.getKey();
      UpodParameter p = ent.getValue();
      Boolean v = Boolean.valueOf(active.contains(st));
      setValueInWorld(p,v,w);
    }
}




/********************************************************************************/
/*                                                                              */
/*      Action Support classes                                                  */
/*                                                                              */
/********************************************************************************/

private class ConditionCheck implements UpodConditionHandler {
   
   private Transition for_transition;
   
   ConditionCheck(Transition t) {
      for_transition = t;
    }
   
   @Override public void conditionError(UpodWorld w,UpodCondition c,Throwable cause) {
    }
   
   @Override public void conditionOff(UpodWorld w,UpodCondition c) {
    }
   
   @Override public void conditionOn(UpodWorld w,UpodCondition c,UpodPropertySet ps) {
      applyTransition(w,for_transition);
    }
   
   @Override public void conditionTrigger(UpodWorld w,UpodCondition c,UpodPropertySet ps) {
      applyTransition(w,for_transition);
    }
   
}       // end of inner class ConditionCheck


 
private class TimeCheck extends TimerTask {
    
   @Override public void run() {
      next_check = null;
      UpodWorld w = getCurrentWorld();
      updateTime(w);
    }
   
}       // end of inner class TimeCheck

/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override protected void outputLocalXml(IvyXmlWriter xw)
{
   for (Transition t : all_transitions) {
      t.outputXml(xw);
    }
}


/********************************************************************************/
/*										*/
/*	Transition information							*/
/*										*/
/********************************************************************************/

private class Transition {

   private String	 from_state;
   private UpodCondition on_condition;
   private long 	 after_time;
   private String	 to_state;
 
   Transition(String from,UpodCondition c,String state) {
      from_state = from;
      on_condition = c;
      to_state = state;
      after_time = 0;
    }

   Transition(String from,long delay,String to) {
      from_state = from;
      after_time = delay;
      on_condition = null;
      to_state = to;
    }
   
   Transition(Element xml) {
      from_state = IvyXml.getAttrString(xml,"FROM");
      to_state = IvyXml.getAttrString(xml,"TO");
      after_time = IvyXml.getAttrLong(xml,"AFTER",0);
      String cname = IvyXml.getAttrString(xml,"COND");
      if (cname != null) on_condition = getUniverse().findBasicCondition(cname);
    }

   String getFromState()                { return from_state; }
   String getToState()                  { return to_state; }
   UpodCondition getCondition()         { return on_condition; }
   long getDelayTime()                  { return after_time; }
   
   void outputXml(IvyXmlWriter xw) {
      xw.begin("TRANSITION");
      xw.field("FROM",from_state);
      xw.field("TO",to_state);
      if (after_time > 0) xw.field("AFTER",after_time);
      if (on_condition != null) {
         xw.field("COND",on_condition.getName());
       }
      xw.end("TRANSITION");  
    }
   
}	// end of inner class Transition



/********************************************************************************/
/*										*/
/*	FSA instance								*/
/*										*/
/********************************************************************************/

private class Instance {

   private String	current_state;
   private long 	enter_time;

   Instance() {
      current_state = start_name;
      enter_time = System.currentTimeMillis();
    }
   
   boolean applyTransition(Transition t) {
      if (!inState(t.getFromState())) return false;
      
      current_state = t.getToState();
      enter_time = System.currentTimeMillis();
      return true;
    }
   
   void handleTime(long when) {
      for (Transition t : all_transitions) {
         long delay = t.getDelayTime();
         if (delay <= 0) continue;
         if (!inState(t.getFromState())) continue;
         if (enter_time + delay > when) continue;
         applyTransition(t); 
         break;
       }
    }
   
   boolean inState(String state) {
      if (state == null || state.equals(ANY_STATE)) return true;
      if (state.equals(current_state)) return true;
      return false;
    }
   
   long getEnterTime()                  { return enter_time; }
   String getCurrentState()             { return current_state; }
   
   boolean isActive() {
      if (current_state == null || current_state.equals(START_STATE)) return false;
      return true;
    }

}	// end of inner class Instance




}	// end of class BasisSensorFsa




/* end of BasisSensorFsa.java */

