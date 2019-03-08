/********************************************************************************/
/*										*/
/*		UmonCommand.java						*/
/*										*/
/*	Holder for a command							*/
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



package edu.brown.cs.upod.umon;

import edu.brown.cs.upod.basis.*;
import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.*;

import java.io.IOException;
import java.text.*;
import java.util.*;


abstract class UmonCommand implements UmonConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,Object>	command_params;
protected UmonControl		umon_control;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected UmonCommand(UmonControl ctrl)
{
   umon_control = ctrl;
   command_params = new HashMap<String,Object>();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getParameter(String nm)
{
   Object o = command_params.get(nm);
   if (o == null) return null;
   return o.toString();
}

int getIntParameter(String nm,int dflt)
{
   Object o = command_params.get(nm);
   if (o == null) return dflt;
   if (o instanceof Number) {
      Number n = (Number) o;
      return n.intValue();
    }
   if (o instanceof Boolean) {
      Boolean v = (Boolean) o;
      return (v ? 1 : 0);
    }
   try {
      return Integer.parseInt(o.toString());
    }
   catch (NumberFormatException e) { }
   return dflt;
}


boolean getBooleanParameter(String nm)
{
   Object o = command_params.get(nm);
   if (o == null) return false;
   if (o instanceof Boolean) {
      return (Boolean) o;
    }
   if (o instanceof Number) {
      Number n = (Number) o;
      return n.intValue() != 0;
    }
   String v = o.toString();
   if (v.length() == 0) return false;
   char c = v.charAt(0);
   if (c == 'T' || c == 't' || c == '1' || c == 'Y' || c == 'y') return true;
   return false;
}


protected void setParameter(String nm,Object value)
{
   command_params.put(nm,value);
}


boolean isAllowed(UpodAccess.Role role)
{
   return true;
}



protected boolean requireRead(UpodAccess.Role r)
{
   switch (r) {
      case READ :
      case WRITE :
         return true;
      default :
         return false;
    }
}


protected boolean requireWrite(UpodAccess.Role r)
{
   switch (r) {
      case WRITE :
         return true;
      default :
         return false;
    }
}




/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

abstract String evaluate() throws UmonException;




/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/



protected void outputWorld(UpodWorld w,IvyXmlWriter xw)
{
   if (!w.isCurrent()) {
      umon_control.getProgram().runOnce(w,null);
    }

   xw.begin("WORLD");
   xw.field("ID",w.getUID());
   xw.field("CURRENT",w.isCurrent());
   xw.field("TIME",w.getTime());
   for (UpodDevice ue : w.getUniverse().getDevices()) {
      xw.begin("DEVICE");
      xw.field("ID",ue.getUID());
      xw.field("NAME",ue.getName());
      xw.field("LABEL",ue.getLabel());
      for (UpodParameter up : ue.getParameters()) {
	 Object val = w.getValue(up);
	 if (val != null) {
	    up.outputXml(xw,val);
	  }
       }
      xw.end("DEVICE");
    }
   xw.end("WORLD");
}




/********************************************************************************/
/*										*/
/*	Command parsing methods 						*/
/*										*/
/********************************************************************************/

static public UmonCommand createCommand(UmonControl ctrl,Element xml)
{
   CommandName cn;

   if (IvyXml.isElement(xml,"UPOD")) {
      cn = IvyXml.getAttrEnum(xml,"COMMAND",CommandName.NONE);
    }
   else {
      String nn = xml.getNodeName();
      try {
	 cn = CommandName.valueOf(nn);
       }
      catch (IllegalArgumentException e)  {
	 cn = CommandName.NONE;
       }
    }

   try {
      switch (cn) {
	 case PING :
	    return new PingCommand(ctrl);
	 case STOP :
	    return new StopCommand(ctrl);
	 case LIST :
	    return new ListCommand(ctrl,xml);
	 case LIST_PROGRAM :
	    return new ListProgramCommand(ctrl);
         case LIST_RESTRICT :
            return new ListRestrictCommand(ctrl,xml);
	 case LIBRARY :
	    return new LibraryCommand(ctrl,xml);
	 case ADD_RULE :
	    return new AddRuleCommand(ctrl,xml);
	 case CHANGE_RULE_PRIORITY :
	    return new ChangeRulePriorityCommand(ctrl,xml);
	 case REMOVE_RULE :
	    return new RemoveRuleCommand(ctrl,xml);
	 case SET_SENSOR :
	    return new SetSensorCommand(ctrl,xml);
	 case DO_ACTION :
	    return new DoActionCommand(ctrl,xml);
	 case CREATE_WORLD :
	    return new CreateWorldCommand(ctrl,xml);
	 case REMOVE_WORLD :
	    return new RemoveWorldCommand(ctrl,xml);
	 case SET_TIME :
	    return new SetTimeCommand(ctrl,xml);
	 case DEDUCE_RULE :
	    return new DeduceRuleCommand(ctrl,xml);
       }
    }
   catch (Throwable e) {
      BasisLogger.log("Illegal command: " + IvyXml.convertXmlToString(xml));
      e.printStackTrace();
    }

   return null;
}


static public UmonCommand createStopCommand(UmonControl uc)
{
   return new StopCommand(uc);
}


static public UmonCommand createCreateWorldCommand(UmonControl uc,String wid)
{
   return new CreateWorldCommand(uc,wid);
}

static public UmonCommand createRemoveWorldCommand(UmonControl uc,String wid)
{
   return new RemoveWorldCommand(uc,wid);
}

static public UmonCommand createListWorldCommand(UmonControl uc,String wid)
{
   return new ListCommand(uc,wid);
}

static public UmonCommand createSetTimeCommand(UmonControl uc,String wid,long time)
{
   return new SetTimeCommand(uc,wid,time);
}

static public UmonCommand createListRestrictCommand(UmonControl uc,String sensor,String entity,
      String cond)
{
   Element celt = IvyXml.convertStringToXml(cond);
   return new ListRestrictCommand(uc,sensor,entity,celt);
}

static public UmonCommand createDoActionCommand(UmonControl uc,String wid,String act,
      Map<String,String> parms)
{
   Element eact = IvyXml.convertStringToXml(act);;
   return new DoActionCommand(uc,wid,eact,parms);
}

static public UmonCommand createSetSensorCommand(UmonControl uc,String wid,String did,
      String param,String state) 
{
   return new SetSensorCommand(uc,wid,did,param,state);
}

static public UmonCommand createListProgramCommand(UmonControl uc)
{
   return new ListProgramCommand(uc);
}

static public UmonCommand createAddRuleCommand(UmonControl uc,String rid,String newrule)
{
   Element xml = IvyXml.convertStringToXml(newrule);
   return new AddRuleCommand(uc,rid,xml);
}

static public UmonCommand createChangeRulePriorityCommand(UmonControl uc,String rid,double pri)
{
   return new ChangeRulePriorityCommand(uc,rid,pri);
}

static public UmonCommand createRemoveRuleCommand(UmonControl uc,String rid)
{
   return new RemoveRuleCommand(uc,rid);
}

static public UmonCommand createDeduceRuleCommand(UmonControl uc,String wid,String date,String time,
      String parameters)
{
   Element pelt = IvyXml.convertStringToXml(parameters);
   return new DeduceRuleCommand(uc,wid,date,time,pelt);
}

static public UmonCommand createNewLatchSensorCommand(UmonControl uc,String name,String did,
      String pid,String state,String cond,long r,long a,String time)
{
   return new NewLatchSensorCommand(uc,name,did,pid,state,cond,r,a,time);
}

static public UmonCommand createNewTimedSensorCommand(UmonControl uc,String name,String did,
      String pid,String state,String cond,long s,long e)
{
   return new NewTimedSensorCommand(uc,name,did,pid,state,cond,s,e);
}


static public UmonCommand createNewFsaSensorCommand(UmonControl uc,String name,String trans)
{
   Element telt = IvyXml.convertStringToXml(trans);
   return new NewFsaSensorCommand(uc,name,telt);
}


static public UmonCommand createNewOrSensorCommand(UmonControl uc,String name,String conds)
{
   Element celt = IvyXml.convertStringToXml(conds);
   return new NewOrSensorCommand(uc,name,celt);
}

static public UmonCommand createEnableCommand(UmonControl uc,String did,boolean enable)
{
   return new EnableDeviceCommand(uc,did,enable);
}

/********************************************************************************/
/*										*/
/*	PING command								*/
/*										*/
/********************************************************************************/

private static class PingCommand extends UmonCommand {

   PingCommand(UmonControl uc) {
      super(uc);
   }

   @Override String evaluate() {
      return "<PONG/>";
    }

}	// end of inner class PingCommand



/********************************************************************************/
/*										*/
/*	STOP command								*/
/*										*/
/********************************************************************************/

private static class StopCommand extends UmonCommand {

   StopCommand(UmonControl uc) {
      super(uc);
    }

   @Override String evaluate() {
      umon_control.stop();
      return "<STOP/>";
    }

}	// end of inner class StopCommand



/********************************************************************************/
/*										*/
/*	LIST command								*/
/*										*/
/********************************************************************************/

private static class ListCommand extends UmonCommand {

   private UpodWorld for_world;

   ListCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      String wid = IvyXml.getTextElement(xml,"WORLD");
      for_world = null;
      if (wid != null) for_world = pgm.getWorld(wid);
   }
   
   ListCommand(UmonControl ctrl,String wid) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      for_world = null;
      if (wid != null) for_world = pgm.getWorld(wid);
   }

   @Override String evaluate() {
      IvyXmlWriter xw = new IvyXmlWriter();
      if (for_world != null) {
         outputWorld(for_world,xw);
       }
      else {
          outputUniverse(umon_control.getUniverse(),xw);
        }
   
      return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireRead(r); }
   
   private void outputUniverse(UpodUniverse uu,IvyXmlWriter xw){
      xw.begin("UNIVERSE");
      xw.field("IDENTITY",uu.getIdentity());
      xw.field("NAME",uu.getName());
      xw.field("LABEL",uu.getLabel());
      xw.begin("DEVICES");
      for (UpodDevice ue : uu.getDevices()) {
         ue.outputXml(xw);
       }
      xw.end("DEVICES");
      xw.begin("BASICCONDS");
      for (UpodCondition uc : uu.getBasicConditions()) {
         if (uc.getLabel() != null && uc.getLabel().length() > 0)
            uc.outputXml(xw);
       }
      xw.end("BASICCONDS");
      UpodProgram ur = umon_control.getProgram();
      ur.outputXml(xw);
      xw.end("UNIVERSE");
    }



}	// end of inner class ListCommand




/********************************************************************************/
/*										*/
/*	RULE commands								*/
/*										*/
/********************************************************************************/

private static class AddRuleCommand extends UmonCommand {

   private UpodRule	old_rule;
   private UpodRule	new_rule;

   AddRuleCommand(UmonControl ctrl,Element xml){
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      old_rule = null;
      String oid = IvyXml.getAttrString(xml,"REPLACE");
      old_rule = pgm.findRule(oid);
      new_rule = pgm.createRule(IvyXml.getChild(xml,"RULE"));
      if (old_rule != null) new_rule.setPriority(old_rule.getPriority());
    }
   
   AddRuleCommand(UmonControl ctrl,String oid,Element rule){
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      old_rule = pgm.findRule(oid);;
      new_rule = pgm.createRule(rule);
      if (old_rule != null) new_rule.setPriority(old_rule.getPriority());
   }

   @Override String evaluate() throws UmonException {
      if (new_rule == null)  throw new UmonException("Illegal rule");
      UpodProgram pgm = umon_control.getProgram();
      pgm.addRule(new_rule);
      if (old_rule != null) pgm.removeRule(old_rule);
      umon_control.saveProgram();
   
      pgm.runOnce(null,null);
   
      IvyXmlWriter xw = new IvyXmlWriter();
      new_rule.outputXml(xw);
      return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}	// end of inner class AddRuleCommand



private static class ChangeRulePriorityCommand extends UmonCommand {

   private UpodRule	for_rule;
   private double	rule_priority;

   ChangeRulePriorityCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
   
      Element relt = IvyXml.getChild(xml,"RULE");
      rule_priority = IvyXml.getAttrDouble(xml,"PRIORITY");
      for_rule = pgm.createRule(relt);
      if (for_rule != null &&
            !IvyXml.getAttrString(relt,"ID").equals(for_rule.getUID()))
         for_rule = null;
    }
   
   ChangeRulePriorityCommand(UmonControl ctrl,String rid,double pri) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      rule_priority = pri;
      for_rule = pgm.findRule(rid);
   }

   @Override String evaluate() throws UmonException {
       if (for_rule == null) throw new UmonException("Rule not found");
       if (rule_priority < 0) throw new UmonException("Bad priority");
       UpodProgram pgm = umon_control.getProgram();
       pgm.removeRule(for_rule);
       for_rule.setPriority(rule_priority);
       pgm.addRule(for_rule);
       umon_control.saveProgram();

       pgm.runOnce(null,null);

       IvyXmlWriter xw = new IvyXmlWriter();
       for_rule.outputXml(xw);
       return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}	// end of inner class ChangeRulePriorityCommand



private static class RemoveRuleCommand extends UmonCommand {

   private UpodRule	for_rule;

   RemoveRuleCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      Element relt = IvyXml.getChild(xml,"RULE");
      for_rule = pgm.createRule(relt);
      if (for_rule != null &&
            !IvyXml.getAttrString(relt,"ID").equals(for_rule.getUID()))
         for_rule = null;
    }
   
   RemoveRuleCommand(UmonControl ctrl,String rid) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      for_rule = pgm.findRule(rid);
   }

   @Override String evaluate() throws UmonException {
      if (for_rule == null) throw new UmonException("Rule not found");
      UpodProgram pgm = umon_control.getProgram();
      pgm.removeRule(for_rule);
      umon_control.saveProgram();

      pgm.runOnce(null,null);

      IvyXmlWriter xw = new IvyXmlWriter();
      for_rule.outputXml(xw);
      return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}	// end of inner class RemoveRuleCommand




/********************************************************************************/
/*										*/
/*	Action commands 							*/
/*										*/
/********************************************************************************/

private static class SetSensorCommand extends UmonCommand {

   private String new_state;
   private UpodDevice for_sensor;
   private UpodWorld for_world;
   private UpodParameter for_param;

   SetSensorCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      new_state = IvyXml.getTextElement(xml,"STATE");
      Element selt = IvyXml.getChild(xml,"DEVICE");
      for_world = pgm.getWorld(IvyXml.getTextElement(xml,"WORLD"));
      String pnm = IvyXml.getTextElement(xml,"PARAM");
      for_sensor = pgm.createDevice(selt);
      for_param = for_sensor.findParameter(pnm);
    }
   
   SetSensorCommand(UmonControl ctrl,String wid,String did,String pnm,String state) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      new_state = state;
      for_sensor = ctrl.getUniverse().findDevice(did);
      for_world = pgm.getWorld(wid);
      for_param = for_sensor.findParameter(pnm);
   }

   @Override String evaluate() throws UmonException {
      if (for_sensor == null) throw new UmonException("Sensor not found");
      if (new_state == null) throw new UmonException("No state specified");
      if (for_world == null) throw new UmonException("Illegal world specified");
      Object oldstate = for_sensor.getValueInWorld(for_param,for_world);
      for_sensor.setValueInWorld(for_param,new_state,for_world);
      Object newstate = for_sensor.getValueInWorld(for_param,for_world);
      String oldstr = for_param.unnormalize(oldstate);
      String newstr = for_param.unnormalize(newstate);
      IvyXmlWriter xw = new IvyXmlWriter();
      xw.begin("SETSTATE");
      xw.field("PRIOR",oldstr);
      xw.field("CURRENT",newstr);
      xw.field("WORLD",for_world.getUID());
      for_sensor.outputXml(xw);
      xw.end("SETSTATE");
      return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireRead(r); }
   
}	// end of inner class SetSensorCommand




private static class SetTimeCommand extends UmonCommand {

   private UpodWorld for_world;
   private long set_time;

   SetTimeCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      for_world = pgm.getWorld(IvyXml.getTextElement(xml,"WORLD"));
      set_time = IvyXml.getAttrLong(xml,"TIME");
    }
   
   SetTimeCommand(UmonControl ctrl,String wid,long time) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      for_world = pgm.getWorld(wid);
      set_time = time;
   }

   @Override String evaluate() throws UmonException {
      if (for_world == null) throw new UmonException("Illegal world specified");
      if (set_time <= 0) throw new UmonException("Illegal time specified");

      Calendar c = Calendar.getInstance();
      c.setTimeInMillis(set_time);
      for_world.setTime(c);

      return "<OK/>";
    }
   
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireRead(r); }
   
}



private static class DoActionCommand extends UmonCommand {

   private UpodAction for_action;
   private UpodWorld  for_world;
   private UpodPropertySet input_params;

   DoActionCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      Element aelt = IvyXml.getChild(xml,"ACTION");
      for_action = pgm.createAction(aelt);
      for_world = pgm.getWorld(IvyXml.getTextElement(xml,"WORLD"));
      Element pelt = IvyXml.getChild(xml,"PARAMETERS");
      input_params = pgm.createPropertySet(pelt);
    }
   
   
   DoActionCommand(UmonControl ctrl,String wid,Element aelt,Map<String,String> parms) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      for_action = pgm.createAction(aelt);
      for_world = pgm.getWorld(wid);
      input_params = pgm.createPropertySet(null);
      for (Map.Entry<String,String> ent : parms.entrySet()) {
         input_params.put(ent.getKey(),ent.getValue());
       }
   }

   @Override String evaluate() throws UmonException {
      if (for_action == null) throw new UmonException("No action specified");
      if (for_world == null) throw new UmonException("Illegal world specified");
      for_action.perform(for_world,input_params);
      IvyXmlWriter xw = new IvyXmlWriter();
      for_action.outputXml(xw);
      return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}	// end of inner class DoActionCommand




/********************************************************************************/
/*										*/
/*	World commands								*/
/*										*/
/********************************************************************************/

private static class CreateWorldCommand extends UmonCommand {

   private UpodWorld from_world;

   CreateWorldCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      from_world = pgm.getWorld(IvyXml.getTextElement(xml,"WORLD"));
    }
   
   CreateWorldCommand(UmonControl ctrl,String wid) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      from_world = pgm.getWorld(wid);
   }

   @Override public String evaluate() throws UmonException {
      if (from_world == null) throw new UmonException("Illegal world specified");
      UpodProgram pgm = umon_control.getProgram();
      UpodWorld w = pgm.createWorld(from_world);
      IvyXmlWriter xw = new IvyXmlWriter();
      outputWorld(w,xw);
      return xw.toString();
    }
   
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}	// end of inner class CreateWorldCommand



private static class RemoveWorldCommand extends UmonCommand {

   private UpodWorld old_world;

   RemoveWorldCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      old_world = pgm.getWorld(IvyXml.getTextElement(xml,"WORLD"));
    }
   
   RemoveWorldCommand(UmonControl ctrl,String wid) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      old_world = pgm.getWorld(wid);
   }

   @Override public String evaluate() throws UmonException {
      if (old_world == null) throw new UmonException("Illegal world specified");
      if (old_world.isCurrent())
	 throw new UmonException("Can't remove current world");

      UpodProgram pgm = umon_control.getProgram();
      pgm.removeWorld(old_world);
      return null;
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}	// end of inner class RemoveWorldCommand





/********************************************************************************/
/*										*/
/*	LIST_PROGRAM command							*/
/*										*/
/********************************************************************************/

private static class ListProgramCommand extends UmonCommand {

   ListProgramCommand(UmonControl ctrl) {
      super(ctrl);
    }

   @Override String evaluate() {
      IvyXmlWriter xw = new IvyXmlWriter();
      umon_control.getProgram().outputXml(xw);
      return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireRead(r); }
   
}	// end of inner class ListProgramCommand







/********************************************************************************/
/*                                                                              */
/*      LIST_RESTRICT command                                                   */
/*                                                                              */
/********************************************************************************/

private static class ListRestrictCommand extends UmonCommand {
   
   private UpodDevice sensor_device;
   private UpodDevice entity_device;
   private UpodCondition for_condition;
   
   ListRestrictCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      Element sen = IvyXml.getChild(xml,"SENSOR");
      if (sen == null) sensor_device = null;
      else sensor_device = pgm.createDevice(sen);
      Element ent = IvyXml.getChild(xml,"ENTITY");
      if (ent == null) entity_device = null;
      else entity_device = pgm.createDevice(ent);
      Element relt = IvyXml.getChild(xml,"CONDITION");
      if (relt == null) for_condition = null;
      else for_condition = pgm.createCondition(relt);
    }
   
   ListRestrictCommand(UmonControl ctrl,String sid,String eid,Element relt) {
      super(ctrl);
      UpodProgram pgm = ctrl.getProgram();
      sensor_device = pgm.findDevice(sid);
      entity_device = pgm.findDevice(eid);
      if (relt == null) for_condition = null;
      else for_condition = pgm.createCondition(relt);
    }
   
   
   @Override String evaluate() {
      IvyXmlWriter xw = new IvyXmlWriter();
      UpodProgram pgm = umon_control.getProgram();
      xw.begin("RULES");
      for (UpodRule ur : pgm.getRules()) {
         if (entity_device != null) {
            if (!ur.getDevices().contains(entity_device)) continue;
          }
         if (sensor_device != null) {
            if (!ur.getSensors().contains(sensor_device)) continue;
          }
         if (for_condition != null) {
            UpodCondition uc = ur.getCondition();
            if (!uc.canOverlap(for_condition)) continue;
          }
         ur.outputXml(xw);
       }
      xw.end("RULES");
      String rslt = xw.toString();
      xw.close();
      return rslt;
    }
 
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireRead(r); }
   
}



/********************************************************************************/
/*										*/
/*	Deduce Rule command							*/
/*										*/
/********************************************************************************/

private static class DeduceRuleCommand extends UmonCommand {

   private UpodWorld from_world;
   private UpodProgram for_program;
   private long world_time;
   private BasisRuleDeducer rule_deducer;

   DeduceRuleCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      for_program = ctrl.getProgram();
      from_world = for_program.getWorld(IvyXml.getTextElement(xml,"WORLD"));
      rule_deducer = null;
      
      Element when = IvyXml.getChild(xml,"WHEN");
      String date = IvyXml.getAttrString(when,"DATE");
      String time = IvyXml.getAttrString(when,"TIME");
      DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mma");
      try {
         Date d = df.parse(date + " " + time);
         world_time = d.getTime();
         Calendar c = Calendar.getInstance();
         c.setTime(d);
         if (from_world != null) from_world.setTime(c);
       }
      catch (ParseException e) {
         if (from_world != null)
            world_time = from_world.getTime();
       }
   
      Map<UpodDevice,Map<UpodParameter,Object>> smap = new HashMap<UpodDevice,Map<UpodParameter,Object>>();
      Map<UpodDevice,Map<UpodParameter,Object>> emap = new HashMap<UpodDevice,Map<UpodParameter,Object>>();
      for (Element e : IvyXml.children(xml,"PARAM")) {
         String ptyp = IvyXml.getAttrString(e,"TYPE");
         String pnam = IvyXml.getAttrString(e,"NAME");
         String dnam = IvyXml.getAttrString(e,"DEVICE");
         UpodDevice dev = ctrl.getUniverse().findDevice(dnam);
         if (dev == null) continue;
         UpodParameter param = null;
         param = dev.findParameter(pnam);
         if (param == null) continue;
         String pval = IvyXml.getText(e);
         Object val = param.normalize(pval);
         Map<UpodDevice,Map<UpodParameter,Object>> wmap = null;
         if (ptyp.equals("SENSOR")) wmap = smap;
         else if (ptyp.equals("RESULT")) wmap = emap;
         else continue;
         Map<UpodParameter,Object> xmap = wmap.get(dev);
         if (xmap == null) {
            xmap = new HashMap<UpodParameter,Object>();
            wmap.put(dev,xmap);
          }
         xmap.put(param,val);
       }
      
      rule_deducer = new BasisRuleDeducer(from_world,for_program,smap,emap);
    }
   
   DeduceRuleCommand(UmonControl ctrl,String wid,String date,String time,Element pelt) {
      super(ctrl);
      for_program = ctrl.getProgram();
      from_world = for_program.getWorld(wid);
      rule_deducer = null;
      
      DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mma");
      try {
         Date d = df.parse(date + " " + time);
         world_time = d.getTime();
         Calendar c = Calendar.getInstance();
         c.setTime(d);
         if (from_world != null) from_world.setTime(c);
       }
      catch (ParseException e) {
         if (from_world != null)
            world_time = from_world.getTime();
       }
      
      Map<UpodDevice,Map<UpodParameter,Object>> smap = new HashMap<UpodDevice,Map<UpodParameter,Object>>();
      Map<UpodDevice,Map<UpodParameter,Object>> emap = new HashMap<UpodDevice,Map<UpodParameter,Object>>();
      for (Element e : IvyXml.children(pelt,"PARAM")) {
         String ptyp = IvyXml.getAttrString(e,"TYPE");
         String pnam = IvyXml.getAttrString(e,"NAME");
         String dnam = IvyXml.getAttrString(e,"DEVICE");
         UpodDevice dev = ctrl.getUniverse().findDevice(dnam);
         if (dev == null) continue;
         UpodParameter param = null;
         param = dev.findParameter(pnam);
         if (param == null) continue;
         String pval = IvyXml.getText(e);
         Object val = param.normalize(pval);
         Map<UpodDevice,Map<UpodParameter,Object>> wmap = null;
         if (ptyp.equals("SENSOR")) wmap = smap;
         else if (ptyp.equals("RESULT")) wmap = emap;
         else continue;
         Map<UpodParameter,Object> xmap = wmap.get(dev);
         if (xmap == null) {
            xmap = new HashMap<UpodParameter,Object>();
            wmap.put(dev,xmap);
          }
         xmap.put(param,val);
       }
      
      rule_deducer = new BasisRuleDeducer(from_world,for_program,smap,emap);
   }

   @Override String evaluate() {
      if (from_world == null) return "BAD WORLD";
      Calendar c = Calendar.getInstance();
      c.setTime(new Date(world_time));	
      from_world.setTime(c);
      
      try {
         rule_deducer.outputRule();
       }
      catch (IOException e) { }
      
      rule_deducer.deduceRules();
      
      IvyXmlWriter xw = new IvyXmlWriter();
      for_program.outputXml(xw);
   
      return xw.toString();
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}	// end of inner class DeduceRuleCommand



/********************************************************************************/
/*										*/
/*	Library command 							*/
/*										*/
/********************************************************************************/

private static class LibraryCommand extends UmonCommand {

   private UpodProgram for_program;
   private UpodDevice  for_entity;
   private UpodTransition for_transition;
   private UpodParameter for_property;

   LibraryCommand(UmonControl ctrl,Element xml) {
      super(ctrl);
      for_program = ctrl.getProgram();
      Element ent = IvyXml.getChild(xml,"DEVICE");
      for_entity = for_program.createDevice(ent);
      if (for_entity == null) return;
      Element trns = IvyXml.getChild(xml,"TRANSITION");
      for_transition = null;
      if (trns != null) for_transition = for_program.createTransition(for_entity,trns);
      String pnm = IvyXml.getAttrString(xml,"PROPERTY");
      if (for_transition == null) {
         for (UpodTransition ut : for_entity.getTransitions()) {
            UpodParameter up = ut.getEntityParameter();
            if (up != null && up.getName().equals(pnm) || up.getLabel().equals(pnm)) {
               for_transition = ut;
               for (UpodParameter tup : ut.getParameterSet()) {
        	  if (tup.getParameterType() == up.getParameterType()) {
        	     for_property = tup;
        	     break;
        	   }
        	}
               break;
             }
          }
       }
      else {
         for (UpodParameter up : for_transition.getParameterSet()) {
            if (up.getName().equals(pnm) || up.getLabel().equals(pnm)) {
               for_property = up;
               break;
             }
          }
       }
    }

   @Override String evaluate() {
      IvyXmlWriter xw = new IvyXmlWriter();
      xw.begin("VALUESET");
      Set<Object> values = new HashSet<Object>();
      for (UpodRule ur : for_program.getRules()) {
	 for (UpodAction act : ur.getActions()) {
	    if (act.getDevice() == for_entity) {
	       if (act.getTransition() == for_transition) {
		  UpodParameterSet ups = act.getParameters();
		  String lbl = act.getLabel();
		  Object val = ups.get(for_property);
		  if (val == null || values.contains(val)) continue;
		  values.add(val);
		  xw.begin("ELEMENT");
		  xw.field("LABEL",lbl);
		  xw.cdataElement("VALUE",val.toString());
		  //.add a condensed version of the value here as ICON
		  xw.end("ELEMENT");
		}
	     }
	  }
       }
      xw.end("VALUESET");
      String rslt = xw.toString();
      xw.close();
      return rslt;
    }

   @Override boolean isAllowed(UpodAccess.Role r)       { return requireRead(r); }
   
}	// end of inner class LibraryCommand



/********************************************************************************/
/*                                                                              */
/*      NewLatchSensor command                                                  */
/*                                                                              */
/********************************************************************************/

private static class NewLatchSensorCommand extends UmonCommand {
   
   private String sensor_name;
   private UpodCondition for_condition;
   private UpodDevice for_device;
   private UpodParameter for_parameter;
   private String state_value;
   private long reset_after;
   private long off_after;
   private Calendar reset_time;
   
   NewLatchSensorCommand(UmonControl ctrl,String name,String did,String pid,String state,
         String cond,long r,long a,String time)  {
      super(ctrl);
      sensor_name = name;
      for_condition = ctrl.getUniverse().findBasicCondition(cond);
      for_device = ctrl.getUniverse().findDevice(did);
      if (for_device == null && for_condition == null) return;
      if (for_device != null) {
         for_parameter = for_device.findParameter(pid);
         state_value = state;
       }
      else {
         for_parameter = null;
         state_value = null;
       }
      reset_after = r;
      off_after = a;
      reset_time = null;
      if (reset_after <= 0 && off_after <= 0) {
         if (time == null || time.length() == 0) time = "00:00";
         SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
         reset_time = Calendar.getInstance();
         try {
            Date when = sdf.parse(time);
            reset_time.setTime(when);
          }
         catch (ParseException e) { 
            // reset_time set to midnight by default
          }
       }
    }
   
   @Override String evaluate() throws UmonException {
      UpodDevice rslt = null;
      BasisFactory factory = BasisFactory.getFactory();
      if (for_device != null) {
         if (reset_time != null) {
            rslt = factory.createLatchSensor(sensor_name,for_device,for_parameter,state_value,reset_time);
          }
         else {
            rslt = factory.createLatchSensor(sensor_name,for_device,for_parameter,state_value,
                  reset_after,off_after);
          }
       }
      else if (for_condition != null) {
         if (reset_time != null) {
           rslt = factory.createLatchSensor(sensor_name,for_condition,reset_time);
          }
         else {
            rslt = factory.createLatchSensor(sensor_name,for_condition,reset_after,off_after);
          }
       }
      if (rslt == null) throw new UmonException("Bad latch definition");
      
      BasisUniverse bu = (BasisUniverse) umon_control.getUniverse();
      bu.addDevice(rslt);
      
      IvyXmlWriter xw = new IvyXmlWriter();
      rslt.outputXml(xw);
      return xw.toString();
    }
   
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}



/********************************************************************************/
/*                                                                              */
/*      NewTimedSensor command                                                  */
/*                                                                              */
/********************************************************************************/

private static class NewTimedSensorCommand extends UmonCommand {

   private String sensor_name;
   private UpodCondition for_condition;
   private UpodDevice for_device;
   private UpodParameter for_parameter;
   private String state_value;
   private long start_after;
   private long end_after;
   
   NewTimedSensorCommand(UmonControl ctrl,String name,String did,String pid,String state,
         String cond,long start,long end) {
      super(ctrl);
      sensor_name = name;
      for_condition = ctrl.getUniverse().findBasicCondition(cond);
      for_device = ctrl.getUniverse().findDevice(did);
      if (for_device == null && for_condition == null) return;
      if (for_device != null) {
         for_parameter = for_device.findParameter(pid);
         state_value = state;
       }
      else {
         for_parameter = null;
         state_value = null;
       }
      start_after = start;
      end_after = end;
    }
   
   @Override String evaluate() throws UmonException {
      UpodDevice rslt = null;
      BasisFactory factory = BasisFactory.getFactory();
      if (for_device != null) {
         rslt = factory.createTimedSensor(sensor_name,for_device,for_parameter,state_value,
               start_after,end_after);
       }
      else if (for_condition != null) {
         rslt = factory.createTimedSensor(sensor_name,for_condition,start_after,end_after);
       }
      if (rslt == null) throw new UmonException("Bad duration definition");
      
      BasisUniverse bu = (BasisUniverse) umon_control.getUniverse();
      bu.addDevice(rslt);
      
      IvyXmlWriter xw = new IvyXmlWriter();
      rslt.outputXml(xw);
      return xw.toString();
      
    }
   
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}



/********************************************************************************/
/*                                                                              */
/*      NewFsaSensor command                                                    */
/*                                                                              */
/********************************************************************************/

private static class NewFsaSensorCommand extends UmonCommand {

   private String sensor_name;
   private Element transition_set;
   
   NewFsaSensorCommand(UmonControl ctrl,String name,Element trans)  {
      super(ctrl);
      sensor_name = name;
      transition_set = trans;
    }
   
   @Override String evaluate() throws UmonException {
      BasisFactory factory = BasisFactory.getFactory();
      BasisSensorFsa fsa = (BasisSensorFsa) factory.createAutomataSensor(umon_control.getUniverse(),sensor_name);
      for (Element telt : IvyXml.children(transition_set,"TRANSITION")) {
         String from = IvyXml.getAttrString(telt,"FROM");
         String to = IvyXml.getAttrString(telt,"TO");
         String cname = IvyXml.getAttrString(telt,"COND");
         if (cname != null) {
            UpodCondition cond = umon_control.getUniverse().findBasicCondition(cname);
            if (cond != null) fsa.addTransition(from,cond,to);
            else throw new UmonException("Automata condition not found");
          }
         else {
            long after = IvyXml.getAttrLong(telt,"AFTER",-1);
            if (after > 0) fsa.addTimeout(from,after,to);
            else throw new UmonException("Bad transition defintiion");
          }
       }
      IvyXmlWriter xw = new IvyXmlWriter();
      fsa.outputXml(xw);
      return xw.toString();
    }
   
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}



/********************************************************************************/
/*                                                                              */
/*      NewOrSensor command                                                     */
/*                                                                              */
/********************************************************************************/

private static class NewOrSensorCommand extends UmonCommand {

   private String sensor_name;
   private Element condition_set;
   
   NewOrSensorCommand(UmonControl ctrl,String name,Element conds)  {
      super(ctrl);
      sensor_name = name;
      condition_set = conds;
    }
   
   @Override String evaluate() throws UmonException {
      BasisSensorOr rslt = null;
      for (Element telt : IvyXml.children(condition_set,"CONDITION")) {
         String did = IvyXml.getAttrString(telt,"DEVICE");
         if (did != null) {
            UpodDevice ud = umon_control.getUniverse().findDevice(did);
            String pid = IvyXml.getAttrString(telt,"PARAMETER");
            if (pid == null) pid = ud.getUID();
            UpodParameter up = ud.findParameter(pid);
            String v = IvyXml.getAttrString(telt,"STATE");
            if (rslt == null) {
               rslt = new BasisSensorOr(sensor_name,ud,up,v);
             }
            else rslt.addCondition(ud,up,v);
          }
         else {
            String nm = IvyXml.getAttrString(telt,"NAME");
            UpodCondition uc = umon_control.getUniverse().findBasicCondition(nm);
            if (uc != null) {
               if (rslt == null) rslt = new BasisSensorOr(sensor_name,uc);
               else rslt.addCondition(uc);
             }
          }
       }
      IvyXmlWriter xw = new IvyXmlWriter();
      if (rslt != null) {
         rslt.outputXml(xw);
         rslt.startDevice();
       }
      
      return xw.toString();
    }
   
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}



/********************************************************************************/
/*                                                                              */
/*      Command to enable/disable sensors or devices                            */
/*                                                                              */
/********************************************************************************/

private static class EnableDeviceCommand extends UmonCommand {

   private UpodDevice for_device;
   private boolean enable_flag;
   
   EnableDeviceCommand(UmonControl ctrl,String did,boolean fg)  {
      super(ctrl);
      for_device = ctrl.getUniverse().findDevice(did);
      enable_flag = fg;
    }
   
   @Override String evaluate() throws UmonException {
      if (for_device == null) throw new UmonException("Device not found");
      for_device.enable(enable_flag);
      IvyXmlWriter xw = new IvyXmlWriter();
      for_device.outputXml(xw);
      return xw.toString();
    }
   
   @Override boolean isAllowed(UpodAccess.Role r)       { return requireWrite(r); }
   
}       // end of inner class EnableDeviceCommand


}	// end of class UmonCommand




/* end of UmonCommand.java */

