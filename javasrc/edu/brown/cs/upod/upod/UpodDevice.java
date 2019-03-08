/********************************************************************************/
/*										*/
/*		UpodDevice.java 						*/
/*										*/
/*	Device for UPOD : can be sensor, entity or both 			*/
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

import java.util.*;


public interface UpodDevice extends UpodDescribable, UpodIdentifiable
{



/**
 *	Add a trigger that is called when device changes state.
 **/

void addDeviceHandler(UpodDeviceHandler hdlr);


/**
 *	Remove a trigger.
 **/

void removeDeviceHandler(UpodDeviceHandler hdlr);



/**
 *	Return the set of parameters that can be displayed to show the
 *	state of this entity.  Parameters are used here because they are
 *	typed.	The actual valu8es are in the property set of the world.
 **/

Collection<UpodParameter> getParameters();


/**
 *	Find a parameter by name
 **/

UpodParameter findParameter(String id);

/**
 *	Get the value of a parameter in the given world.  If the world is curernt
 *	this needs to get the current state of the parameter.
 **/

Object getValueInWorld(UpodParameter p,UpodWorld w);


/**
 *	Set the value of a parameter in the given world.  If the world is current,
 *	this will actually affect the device.
 **/

void setValueInWorld(UpodParameter p,Object val,UpodWorld w) throws UpodActionException;


/**
 *	Return the condition associated with a given parameter.  Returns null if
 *	the parameter is not a sensor with an associated condition
 **/

UpodCondition getCondition(UpodParameter p,Object v);


/**
 *	Return the set of basic conditions associated with the device.
 **/

Collection<UpodCondition> getConditions();



/**
 *	Return the transition associated with a given parameter.  Returns null if
 *	the parameter is not a target with an associated transition
 **/

UpodTransition getTransition(UpodParameter p);


/**
 *	Return the set of all transitions for this device
 **/

Collection<UpodTransition> getTransitions();


/**
 *	Indicates if there are any transitions for the device
 **/

boolean hasTransitions();


/**
 *	Find a transition by name
 **/

// UpodTransition findTransition(String name);




/**
 *	Actually apply a transition to the entity in the given world
 **/

void apply(UpodTransition t,UpodPropertySet props,UpodWorld w) throws UpodActionException;


/**
 *	Return the universe associated with this sensor
 **/

UpodUniverse getUniverse();


/**
 *	Enable or disable this device
 **/

void enable(boolean fg);


/**
 *	Check if the device is enabled
 **/

boolean isEnabled();


/**
 *	Return a string describing the current state of the device
 **/

String getCurrentStatus();

String getCurrentStatus(UpodWorld w);






/**
 *	Return the set of groups this device is in
 **/

Collection<String> getGroups();


/**
 *	Return the capabilities of the device
 **/

Collection<UpodCapability> getCapabilities();

/**
 *	Start running the device (after it has been added to universe)
 **/

void startDevice();



/**
 *	Output enough xml to identify the device next time
 **/

void outputXml(IvyXmlWriter xw);




}	// end of interface UpodDevice




/* end of UpodDevice.java */

