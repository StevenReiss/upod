/********************************************************************************/
/*										*/
/*		UpodAction.java 						*/
/*										*/
/*	Action definitions for user-programming of devices			*/
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


import edu.brown.cs.ivy.xml.*;


/**
 *	This interface defines an action that can be triggered by a set of user
 *	specified conditions.
 *
 *	Actions typically releate to setting a particular state of a particular
 *	entity.  The Action interface needs to define what is meant by an entity,
 *	a state, a state change, etc.  It also has to define how we determine when
 *	two actions may be in conflict with one another.
 *
 *	There are a set of standard action implementations.  One is a combination
 *	which implies a set of actions that should be taken simultaneously.
 *
 **/



public interface UpodAction extends UpodDescribable {


/**
 *	Return the entity associated with this action.	Each action refers to
 *	setting a particular state of a particular entity.
 **/

UpodDevice getDevice();



/**
 *	Return the transition associated with a given entity.
 **/

UpodTransition getTransition();



/**
 *	Set the parameters for the transition.  This removes any previous 
 *      parameter associations
 **/

void setParameters(UpodParameterSet params);


/**
 *      Set a non-default description
 **/

void setDescription(String d);


/**
 *      Set a non-default label
 **/
void setLabel(String d);

/**
 *      Get label for external use
 **/
String getLabel();



/**
 *      Set specified set of parameters.  Any non-mentioned parameters are
 *      left untouched.
 **/

void addParameters(UpodParameterSet params);


/**
 *	Get the current parameters.  The returned map is live in that it can
 *	be changed by the caller to change the parameter set
 **/

UpodParameterSet getParameters();




/**
 *      Get the implied properties for rule deduction
 **/

void addImpliedProperties(UpodPropertySet ups);



/**
 *	Perform an action in a hypothetical world.  If the action fails for
 *	some reason (e.g. bad parameters, can't be done), an exception is
 *	returned.  Perform will return once the action is complete.  This
 *	routine is passed an optional set of initial parameters that are
 *	derived from the condition.  This may be null.
 **/

void perform(UpodWorld world,UpodPropertySet inputs) throws UpodActionException;

/**
 *      Output xml so it can be recreated
 **/
void outputXml(IvyXmlWriter xw);



}	// end of interface UpodAction




/* end of UpodAction.java */
