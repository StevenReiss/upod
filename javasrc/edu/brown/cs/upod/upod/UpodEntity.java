/********************************************************************************/
/*										*/
/*		UpodEntity.java 						*/
/*										*/
/*	Entity definitions for user-programming of devices			*/
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
 *	This interface defines an entity.  Entities are the objects upon which
 *	actions occur.	A given system might have a single entity (e.g. the
 *	SmartSign system has only the sign), or might have lots of entities
 *	(e.g. home automation).  Entities can be hierarchical (e.g. all lights
 *	vs a specific light).  Entities also have a set of states that they
 *	support. The internal state is hidden except for determining conflicts.
 **/



public interface UpodEntity extends UpodDescribable {



/**
 *	Return the set of transitions that can be applied to this entity
 **/

Collection<UpodTransition> getApplicableTransitions();




/**
 *	Check if a condition can be applied to an entity in a hypothetical
 *	state which may be the current world state.
 **/

boolean canApply(UpodTransition t,UpodWorld state);



/**
 *      Actually apply a transition to the entity in the given world
 **/

void apply(UpodTransition t,UpodParameterSet parms,UpodWorld w)
        throws UpodActionException;




/**
 *	Get a description of the current state of the entity if it is known.
 **/

String getCurrentState(UpodWorld w);



/**
 *	Return true of the current state of the entity is visible;
 *	return false if not.
 **/

boolean isStateVisible();


/**
 *      Add properties for this entity to a world map.  This should add
 *      the current state of the entity to the world map.
 **/

void addProperties(UpodPropertySet props);



}	// end of interface UpodEntity




/* end of UpodEntity.java */

