/********************************************************************************/
/*										*/
/*		UpodProgram.java						*/
/*										*/
/*	Representation of a runnable UPOD program				*/
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

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXmlWriter;

import java.util.*;


/**
 *	The user interface might want to create hypothetical situations to
 *	determine if there are conflicts or to show the user what would happen
 *	under different conditions.  This interface represents such a state.
 **/


public interface UpodProgram 
{


/**
 *	Return the set of rules in this program in priority order.
 **/

List<UpodRule> getRules();


/**
 *      Return a particular rule by id or name
 **/

UpodRule findRule(String id);

/**
 *      Add a new rule
 **/
void addRule(UpodRule ur);

/**
 *      Remove a rule
 **/
void removeRule(UpodRule ur);


/**
 *      Return the universe of the program
 **/

UpodUniverse getUniverse();



/**
 *	Run the current program once on the current world.  This function
 *	returns true if a rule is triggered.
 **/

boolean runOnce(UpodWorld world,UpodTriggerContext ctx);

/**
 *      Output the program to a file
 **/
void outputXml(IvyXmlWriter xw);

/**
 *      Create (clone) a world
 **/
UpodWorld createWorld(UpodWorld base);


/**
 *      Find world.  The argument is the world UID.  Null can be used
 *      to specify the current world
 **/
UpodWorld getWorld(String uid);


/**
 *      Remove a hypothetical world.
 **/
boolean removeWorld(UpodWorld w);


/**
 *      Method to recreate an action from its XML.
 **/

UpodAction createAction(Element e);


/**
 *      Method to recreate a condition from its XML.
 **/

UpodCondition createCondition(Element e);


/**
 *      Method to find/create an entity from its XMl.
 **/

UpodDevice createDevice(Element e);

UpodDevice findDevice(String id);


/**
 *      Method to create a rule from its XML.
 **/

UpodRule createRule(Element e);





/**
 *      Method to find/create a transition from its XML
 **/

UpodTransition createTransition(UpodDevice ue,Element e);


/**
 *      Create a parameter set from its XML
 **/
UpodParameterSet createParameterSet(Element e);


/**
 *      Create a property set from its XML
 **/
UpodPropertySet createPropertySet(Element e);



}	// end of interface UpodProgram




/* end of UpodProgram.java */

