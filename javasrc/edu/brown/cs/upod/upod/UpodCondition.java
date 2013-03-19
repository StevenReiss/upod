/********************************************************************************/
/*										*/
/*		UpodCondition.java						*/
/*										*/
/*	Condition definitions for user-programming of devices			*/
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






/**
 *	A condition describes when a particular action should be applied.
 *
 *	This describes a standard condition.  There are several special types
 *	of conditions that are supported by the system.  One is time-based
 *	where the condition reflects either a single time (for a trigger) or
 *	a time period.	A second is logical.  This represents an AND (or OR?)
 *	of other conditions.  A third is positional.  This represents the
 *	location of an object or user (and hence can assume properties based
 *	on location).  A fourth is state-based.  This reflects real-world condition
 *	being in a particular state (e.g. the telephone is in use).  A fifth is
 *	time+<condition> based, i.e. this condition has been true (or false) for
 *	a specified amount of time.
 *
 **/



public interface UpodCondition extends UpodDescribable {



/**
 *	Determine if the condition is a trigger or a context.  A trigger is a
 *	one-shot condition (i.e at 2:00pm) while a context is a condition
 *	where the rule should be applied while the context holds (i.e. 2-4pm).
 **/

boolean isTrigger();


/**
 *	Indicates the action is time-based.
 **/

boolean isTimeBased();


/**
 *	Indicates the action is position dependent.
 **/

boolean isPositionBased();


/**
 *	Return the calendar event corresponding to this condition.
 *	This can be used to determine relevance or check for time-
 *	based conflicts.  If this returns null, then the condition
 *	holds at all times.
 **/

UpodCalendarEvent getEventTimes();



/**
 *	Returns the sensor associated with the condition.  If
 *	there is no sensor, returns null.
 **/

UpodSensor getSensor();









/**
 *	poll to check if the condition holds in a given state.	This routine
 *	should return null if the condition does not hold.  If the condition
 *	does hold, it should return a ParameterSet.  The parameter set may
 *	contain values associated with the condition that can later be used
 *	inside the action.
 **/

UpodParameterSet poll(UpodWorld state) throws UpodConditionException;



/**
 *	Register a callback to detect when condition changes
 **/

void addHandler(UpodConditionHandler hdlr);



/**
 *	Remove a registered callback.
 **/

void removeHandler(UpodConditionHandler hdlr);




/**
 *      Return a parameter set describing what parameters if any are needed to
 *      describe this condition.  These will be used to construct a 
 *      parameter set (based on user input) that will be passed to the
 *      proper Condition constructor.  The values associated with each
 *      parameter are the defaults for that parameter
 **/

UpodParameterSet getDefaultParameters();




}	// end of interface UpodCondition




/* end of UpodCondition.java */
