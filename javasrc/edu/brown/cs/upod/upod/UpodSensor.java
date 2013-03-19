/********************************************************************************/
/*										*/
/*		UpodSensor.java 						*/
/*										*/
/*	Representation of a sensor.						*/
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

import java.util.Collection;



/**
 *	A sensor represents an observable object that is accessible to
 *	the system.  It can be used describing conditions or possibly
 *	actions.  It can also just be used to model the state of the
 *	real world.
 **/

public interface UpodSensor extends UpodDescribable
{


/**
 *	Return the set of states of the sensor
 **/

Collection<String> getStates();



/**
 *	Return the current state of the sensor in a hypthetical world.
 **/

String getCurrentState(UpodWorld world);

/**
 *	Add a trigger that is called when sensor changes.
 **/

void addTrigger(UpodSensorHandler hdlr);


/**
 *	Remove a trigger.
 **/

void removeTrigger(UpodSensorHandler hdlr);


/**
 *	Get the basic conditions associated with this trigger.	This
 *	may return an empty list if there are none.
 **/

Collection<UpodCondition> getConditions();


/**
 *      Add properties for this sensor to a world view.
 **/

void addProperties(UpodPropertySet props);



}	// end of interface UpodSensor




/* end of UpodSensor.java */

