/********************************************************************************/
/*                                                                              */
/*              Factory.java                                                    */
/*                                                                              */
/*      description of class                                                    */
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



package edu.brown.cs.upod.spec;


import java.util.*;


/**
 *      This interface serves as a factory for the common entities that need
 *      to be created by the application.
 **/

public interface Factory
{


/**
 *      Create an action for a particular entity and transition.  This
 *      will throw an exception if the transition does not apply to the
 *      entity.
 **/

Action createNewAction(Entity ent,Transition t) throws ActionException;



/**
 *      Create a new rule specifying the given action when the given 
 *      condition holds.  The new rule has the specified priority.
 **/

Rule createNewRule(Condition cond,Action act,double priority);



/**
 *      Return the current world.  This is the default world that reflects
 *      what is really happening at this instant.
 **/

World createCurrentWorld();



/**
 *      Create a logical condition that is the AND of the given set of
 *      conditions.
 ***/

Condition createAndAction(Condition ... act);


/**
 *      Create a logical condition that is the OR of the given set of 
 *      conditions.
 **/

Condition createOrAction(Condition ... act);


/**
 *      Create the not of an condition.  This can throw an exception if the
 *      given action is a trigger and hence is not invertible.
 **/

Condition createNotAction(Condition act) throws ConditionException;



/**
 *      Create a time-based condition.  The parameters should allow creation
 *      or arbitrary calendar-type events (i.e. one shot or repeated, day-based,
 *      trigger or time slot, etc.)
 **/

Condition createTimeCondition(Calendar from,Calendar to) 
        throws ConditionException;   



/**
 *      Create a condition reflecting a particular condition being on for a
 *      given amount of time.
 **/

Condition createTimedCondition(Condition cond,long ontime)
        throws ConditionException;


}       // end of interface Factory




/* end of Factory.java */

