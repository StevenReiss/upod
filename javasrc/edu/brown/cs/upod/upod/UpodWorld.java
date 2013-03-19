/********************************************************************************/
/*										*/
/*		UpodWorld.java							*/
/*										*/
/*	Representation of a hypothetical world state				*/
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



package edu.brown.cs.upod.upod;


import java.util.*;


/**
 *	The user interface might want to create hypothetical situations to
 *	determine if there are conflicts or to show the user what would happen
 *	under different conditions.  This interface represents such a state.
 **/


public interface UpodWorld
{



/**
 *	Tell if this world is the "real world", i.e. is current.
 **/

boolean isCurrent();



/**
 *      Return the associated universe defining the problem
 **/

UpodUniverse getUniverse();


/**
 *	Create a new world that starts out the same as this one
 **/

UpodWorld createClone();


/**
 *	Create an action for a particular entity and transition.  This
 *	will throw an exception if the transition does not apply to the
 *	entity.
 **/

UpodAction createNewAction(UpodEntity ent,UpodTransition t) throws UpodActionException;



/**
 *	Create a new rule specifying the given action when the given
 *	condition holds.  The new rule has the specified priority.
 **/

UpodRule createNewRule(UpodCondition cond,UpodAction act,double priority);



/**
 *	Create a logical condition that is the AND of the given set of
 *	conditions.
 ***/

UpodCondition createAndCondition(UpodCondition ... act);


/**
 *	Create a logical condition that is the OR of the given set of
 *	conditions.
 **/

UpodCondition createOrCondition(UpodCondition ... act);


/**
 *	Create the not of an condition.  This can throw an exception if the
 *	given action is a trigger and hence is not invertible.
 **/

UpodCondition createNotCondition(UpodCondition act) throws UpodConditionException;



/**
 *	Create a time-based condition.	The parameters should allow creation
 *	or arbitrary calendar-type events (i.e. one shot or repeated, day-based,
 *	trigger or time slot, etc.)
 **/

UpodCondition createTimeCondition(Calendar from,Calendar to)
throws UpodConditionException;	



/**
 *	Create a condition reflecting a particular condition being on for a
 *	given amount of time.
 **/

UpodCondition createTimedCondition(UpodCondition cond,long ontime)
throws UpodConditionException;


/**
 *      Add all properties of this world to a property set
 **/

void addProperties(UpodPropertySet ps);


/**
 *      Get the value of a property from the current property set of this 
 *      world.  This is undefined for the current world.
 **/
Object getProperty(String prop);


/**
 *      Set a property for the current world.  This will throw an exception 
 *      if used for the current world.
 **/
void setProperty(String prop,Object value);



/**
 *	Return the set of available entities that can be acted upon.
 **/

Collection<UpodEntity> getEntities();



/**
 *	Return the set of available sensors.
 **/

Collection<UpodSensor> getSensors();



}	// end of interface UpodWorld




/* end of UpodWorld.java */

