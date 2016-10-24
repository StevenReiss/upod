/********************************************************************************/
/*										*/
/*		BasisCondition.java						*/
/*										*/
/*	Basic implementation of a condition					*/
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

import edu.brown.cs.ivy.swing.*;
import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.*;

import java.util.*;


public abstract class BasisCondition implements UpodCondition, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

enum State { OFF, ON, ERROR };

private Map<UpodWorld,CondState>	state_map;
private SwingEventListenerList<UpodConditionHandler> condition_handlers;
private String			condition_label;
private UpodUniverse		for_universe;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisCondition(UpodUniverse uu)
{
   for_universe = uu;
   state_map = new HashMap<UpodWorld,CondState>();
   condition_handlers = new SwingEventListenerList<UpodConditionHandler>(
	 UpodConditionHandler.class);
   condition_label = null;
}

protected BasisCondition(UpodProgram pgm,Element xml)
{
   this(pgm.getUniverse());
   condition_label = Coder.unescape(IvyXml.getTextElement(xml,"LABEL"));
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override abstract public String getName();
@Override abstract public String getDescription();

@Override public UpodUniverse getUniverse()		{ return for_universe; }
@Override public String getLabel()
{
   return condition_label;
}

@Override public void setLabel(String s)
{
   condition_label = s;
}


@Override public UpodParameterSet getDefaultParameters()	{ return null; }

@Override public UpodParameterSet getParameters()		{ return null; }



@Override public void addConditionHandler(UpodConditionHandler hdlr)
{
   condition_handlers.add(hdlr);
}

@Override public void removeConditionHandler(UpodConditionHandler hdlr)
{
   condition_handlers.remove(hdlr);
}


@Override public boolean isTrigger()				{ return false; }



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

@Override public final UpodPropertySet getCurrentStatus(UpodWorld world)
	throws UpodConditionException
{
   setTime(world);

   CondState cs = getState(world);

   UpodConditionException cex = cs.getError();
   if (cex != null) throw cex;

   return cs.getProperties();
}


@Override public abstract void setTime(UpodWorld w);



private synchronized CondState getState(UpodWorld w)
{
   CondState cs = state_map.get(w);
   if (cs == null) {
      cs = new CondState();
      state_map.put(w,cs);
    }
   return cs;
}


/********************************************************************************/
/*										*/
/*	Trigger methods 							*/
/*										*/
/********************************************************************************/

public void fireOn(UpodWorld w,UpodPropertySet input)
{
   if (input == null) input = new BasisPropertySet();

   w.startUpdate();
   try {
      CondState cs = getState(w);
      if (!cs.setOn(input)) return;

      for (UpodConditionHandler ch : condition_handlers) {
	 try {
	    ch.conditionOn(w,this,input);
	  }
	 catch (Throwable t) {
	    BasisLogger.logE("Problem with condition handler",t);
	  }
       }
    }
   finally {
      w.endUpdate();
    }
}


public void fireTrigger(UpodWorld w,UpodPropertySet input)
{
   if (input == null) input = new BasisPropertySet();

   w.startUpdate();
   try {
      for (UpodConditionHandler ch : condition_handlers) {
	 try {
	    ch.conditionTrigger(w,this,input);
	  }
	 catch (Throwable t) {
	    BasisLogger.logE("Problem with condition handler",t);
	  }
       }
    }
   finally {
      w.endUpdate();
    }
}

public void fireOff(UpodWorld w)
{
   w.startUpdate();
   try {
      CondState cs = getState(w);
      if (!cs.setOff()) return;

      for(UpodConditionHandler ch : condition_handlers) {
	 try {
	    ch.conditionOff(w,this);
	  }
	 catch (Throwable t) {
	    BasisLogger.logE("Problem with condition handler",t);
	  }
       }
    }
   finally {
      w.endUpdate();
    }
}



public void fireError(UpodWorld w,Throwable cause)
{
   w.startUpdate();
   try {
      CondState cs = getState(w);
      if (!cs.setError(cause)) return;

      for (UpodConditionHandler ch : condition_handlers) {
	 try {
	    ch.conditionError(w,this,cause);
	  }
	 catch (Throwable t) {
	    BasisLogger.logE("Problem with condition handler",t);
	  }
       }
    }
   finally {
      w.endUpdate();
    }
}



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public abstract void outputXml(IvyXmlWriter xw);


protected void outputHeader(IvyXmlWriter xw)
{
   xw.begin("CONDITION");
   xw.field("CLASS",getClass().getName());
   xw.field("NAME",getName());
   xw.field("TRIGGER",isTrigger());
   xw.field("LABEL",getLabel());
}


protected void outputTrailer(IvyXmlWriter xw)
{
   xw.textElement("DESC",Coder.escape(getDescription()));

   UpodParameterSet ps = getParameters();

   if (ps != null) {
      for (Map.Entry<UpodParameter,Object> ent : ps.entrySet()) {
	 xw.begin("PARAMETER");
	 xw.field("NAME",ent.getKey().getName());
	 xw.text(ent.getValue().toString());
	 xw.end("PARAMETER");
       }
    }

   xw.end("CONDITION");
}



/********************************************************************************/
/*										*/
/*	   flict checking							*/
/*										*/
/********************************************************************************/

@Override public boolean canOverlap(UpodCondition uc)
{
   if (uc == null) return true;

   BasisCondition bc = (BasisCondition) uc;

   return bc.checkOverlapConditions(this);
}


protected boolean checkOverlapConditions(BasisCondition uc)
{
   return uc.isConsistentWith(this);
}



protected abstract boolean isConsistentWith(BasisCondition uc);


@Override public abstract void addImpliedProperties(UpodPropertySet ups);




/********************************************************************************/
/*										*/
/*	State Tracking								*/
/*										*/
/********************************************************************************/

private static class CondState {

   private UpodPropertySet on_parameters;
   private UpodConditionException   error_condition;

   CondState() {
      on_parameters = null;
      error_condition = null;
    }

   boolean setOn(UpodPropertySet ps) {
      if (on_parameters != null && on_parameters.equals(ps)) return false;
      error_condition = null;
      on_parameters = new BasisPropertySet(ps);
      return true;
    }

   boolean setError(Throwable t) {
      if (error_condition != null && error_condition.equals(t)) return false;
      if (t instanceof UpodConditionException)
	 error_condition = (UpodConditionException) t;
      else
	 error_condition = new UpodConditionException("Condition aborted",t);
      on_parameters = null;
      return true;
    }

   boolean setOff() {
      if (error_condition == null && on_parameters == null) return false;
      error_condition = null;
      on_parameters = null;
      return true;
    }


   UpodPropertySet getProperties()		{ return on_parameters; }

   UpodConditionException getError()		{ return error_condition; }

}	// end of inner class CondState


}	// end of class BasisCondition




/* end of BasisCondition.java */

