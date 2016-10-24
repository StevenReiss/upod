/********************************************************************************/
/*										*/
/*		UpodTransition.java						*/
/*										*/
/*	Transition definitions for user-programming of devices			*/
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

import java.util.Collection;






/**
 *	A transition describes a potential action or change request for a
 *	particular entity.
 **/



public interface UpodTransition extends UpodDescribable {



/**
 *	Get the set of parameters associated with this transition.
 **/

Collection<UpodParameter> getParameterSet();


/**
 *      Get the entity parameter set by this transition if any
 **/

UpodParameter getEntityParameter();



/**
 *	Return the external label
 **/
String getLabel();


/**
 *	Find a parameter by name
 **/

UpodParameter findParameter(String nm);

/**
 *	Get the default parameter values for this transition. This returns a
 *	copy of the default parameters that the caller is free to change.
 **/

UpodParameterSet getDefaultParameters();



/**
 *      Indicate whether this transition is a trigger or a more permanent setting
 **/

enum Type {
   STATE_CHANGE,                // changes state until another event
   TEMPORARY_CHANGE,            // changes state, device will reset by itselft
   TRIGGER                      // triggers something, no state change
}

Type getTransitionType();

/**
 *	Execute the transition on the given world.
 **/

void perform(UpodWorld w,UpodDevice e,UpodPropertySet p)
	throws UpodActionException;


/**
 *	Output xml so that the transition can be found again
 **/

void outputXml(IvyXmlWriter xw);



}	// end of interface UpodTransition



/* end of UpodTransition.java */
