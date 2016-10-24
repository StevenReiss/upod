/********************************************************************************/
/*										*/
/*		BasisFactory.java						*/
/*										*/
/*	Basic factory class							*/
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

import java.util.*;


public class BasisFactory implements UpodFactory, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Map<UpodUniverse,UpodWorld>	current_worlds;

private static BasisFactory		the_factory = new BasisFactory();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public static BasisFactory getFactory() 	{ return the_factory; }

private BasisFactory()
{
   current_worlds = new HashMap<UpodUniverse,UpodWorld>();
}



/********************************************************************************/
/*										*/
/*	Create the current world						*/
/*										*/
/********************************************************************************/

@Override public synchronized UpodWorld getCurrentWorld(UpodUniverse uu)
{
   if (uu == null) throw new IllegalArgumentException("Empty universe");

   UpodWorld uw = current_worlds.get(uu);
   if (uw == null) {
      uw = new BasisWorldCurrent(uu);
      current_worlds.put(uu,uw);
    }

   return uw;
}



/********************************************************************************/
/*										*/
/*	Methods for creating conditions 					*/
/*										*/
/********************************************************************************/

@Override public UpodCondition createAndCondition(UpodCondition ... act) throws UpodConditionException
{
   return new BasisConditionLogical.And(act);
}


@Override public UpodCondition createOrCondition(UpodCondition ... act) throws UpodConditionException
{
   return new BasisConditionLogical.Or(act);
}



@Override public UpodCondition createTimeCondition(UpodUniverse uu,String nm,Calendar from,Calendar to)
{
   BasisCalendarEvent ce = new BasisCalendarEvent(from,to);
   BasisConditionTime cc = new BasisConditionTime(uu,nm,ce);

   return cc;
}

@Override public UpodCondition createTimedCondition(UpodCondition cond,
      long starttime,long endtime)
{
   return new BasisConditionDuration(cond,starttime,endtime,false);
}






/********************************************************************************/
/*										*/
/*	Sensor creation 							*/
/*										*/
/********************************************************************************/

@Override public UpodDevice createTimedSensor(String id,UpodDevice base,UpodParameter param,
      Object state,long start,long end)
{
   return new BasisSensorDuration(id,base,param,state,start,end);
}


@Override public UpodDevice createTimedSensor(String id,UpodCondition cond,
      long start,long end)
{
   return new BasisSensorDuration(id,cond,start,end);
}


@Override public UpodDevice createLatchSensor(String id,
      UpodDevice base,UpodParameter param,
      Object state,Calendar reset)
{
   return new BasisSensorLatch(id,base,param,state,reset);
}


@Override public UpodDevice createLatchSensor(String id,
      UpodDevice base,UpodParameter param,
      Object state,long reset,long offafter)
{
   return new BasisSensorLatch(id,base,param,state,reset,offafter);
}


@Override public UpodDevice createLatchSensor(String id,
      UpodCondition cond,long reset,long offafter)
{
   return new BasisSensorLatch(id,cond,reset,offafter);
}


@Override public UpodDevice createLatchSensor(String id,
      UpodCondition cond,Calendar reset)
{
   return new BasisSensorLatch(id,cond,reset);
}



@Override public UpodDevice createAutomataSensor(UpodUniverse uu,String id)
{
   return new BasisSensorFsa(uu,id);
}


@Override public UpodDevice createOrSensor(String id,
      UpodDevice base,UpodParameter p,Object state)
{
   return new BasisSensorOr(id,base,p,state);
}


/********************************************************************************/
/*										*/
/*	Methods for creating rules						*/
/*										*/
/********************************************************************************/

@Override public UpodRule createNewRule(UpodCondition c,
      List<UpodAction> acts,double pr)
{
   BasisRule br = new BasisRule(c,acts,null,pr);
   return br;
}


/********************************************************************************/
/*										*/
/*	Methods for creating actions						*/
/*										*/
/********************************************************************************/

@Override public UpodAction createNewAction(UpodDevice ent,UpodTransition t)
{
   BasisAction ba = new BasisAction(ent,t);
   return ba;
}















}	// end of class BasisFactory




/* end of BasisFactory.java */

