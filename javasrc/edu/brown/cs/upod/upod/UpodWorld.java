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
import edu.brown.cs.ivy.xml.IvyXmlWriter;



/**
 *	The user interface might want to create hypothetical situations to
 *	determine if there are conflicts or to show the user what would happen
 *	under different conditions.  This interface represents such a state.
 **/


public interface UpodWorld extends UpodIdentifiable
{



/**
 *	Tell if this world is the "real world", i.e. is current.
 **/

boolean isCurrent();

/**
 *      Create a clone of this world
 **/
UpodWorld createClone();


/**
 *      Return the associated universe defining the problem
 **/

UpodUniverse getUniverse();




/**
*       Get the set of all parameters
**/

UpodParameterSet getParameters();



/**
 *      Get the value of a property from the current property set of this 
 *      world.  This is undefined for the current world.
 **/
Object getValue(UpodParameter p);

void setValue(UpodParameter p,Object v);




/**
 *      Set the time for this world.  This will throw an exception for the
 *      current world.  If the world has a time range, this will only set times
 *      within that range.
 **/

void setTime(Calendar time);



/**
 *      Return the current time in the world.
 **/

long getTime();



/**
 *      Return the current time
 **/

Calendar getCurrentTime();



/********************************************************************************/
/*                                                                              */
/*      Updating methods                                                        */
/*                                                                              */
/********************************************************************************/

void addTrigger(UpodCondition c,UpodPropertySet ps);

void startUpdate();

void endUpdate();

UpodTriggerContext waitForUpdate();

void updateLock();
void updateUnlock();

/**
 *      Output world information 
 **/
void outputXml(IvyXmlWriter xw);


}	// end of interface UpodWorld




/* end of UpodWorld.java */

