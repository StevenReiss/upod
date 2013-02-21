/********************************************************************************/
/*										*/
/*		Transition.java 						*/
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





package edu.brown.cs.upod.spec;


import java.util.Collection;





/**
 *	A transition describes a potential action or change request for a
 *      particular entity.
 **/



public interface Transition extends Describable {



/**
 *	Determine if the state change associated with this action is visible to the
 *	the system and hence if the change can be checked.
 **/

boolean isCheckable();



/**
 *	Determine the timing characteristics of the transition.  A timing of 0 
 *      implies the transition is permanent (i.e. holds until another transition 
 *      resets it.)  A timing of -1 implies the state change is a trigger that 
 *      doesn't really affect the underlying state.  A timing greater than 0  
 *      indicates that the transition is temporary for time milliseconds.
 **/

long getTransitionTiming();


/**
 *      Return true if the transition is a trigger (i.e. one shot,
 *      doesn't change state, just does a quick action); returns
 *      false otherwise.
 **/

boolean isTrigger();
   



/**
 *      Get the set of parameters associated with this transition.  
 **/

Collection<Parameter> getParameterSet();

/**
 *      Get the default parameter values for this transition. This returns a 
 *      copy of the default parameters that the caller is free to change. 
 **/

ParameterSet getDefaultParameters();



/**
 *      Execute a transition synchronously in a hypothetical world.
 **/

void perform(World world,Entity entity,ParameterSet parameters) 
        throws ActionException;



/**
 *      Execute a transition asynchonously.  The passed in World must be
 *      the current world.
 **/

void performAsync(World w,StatusHandler sts,Entity e,ParameterSet p) 
        throws ActionException;



}	// end of interface Transition



/* end of Transition.java */
