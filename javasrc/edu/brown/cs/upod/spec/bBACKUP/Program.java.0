/********************************************************************************/
/*                                                                              */
/*              World.java                                                      */
/*                                                                              */
/*      Representation of a hypothetical world state                            */
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
 *      The user interface might want to create hypothetical situations to 
 *      determine if there are conflicts or to show the user what would happen
 *      under different conditions.  This interface represents such a state.
 **/


public interface World extends Describable
{



/**
 *      Tell if this world is the "real world", i.e. is current.
 **/

boolean isCurrent();



/**
 *      Create a new world that starts out the same as this one
 **/

World createClone();


/**
 *      Return the set of available primitive conditions
 **/

Collection<Condition> getPrimitiveConditions();


/**
 *      Return the set of available entities that can be acted upon.
 **/

Collection<Entity> getEntities();



/**
 *      Return the set of available sensors.
 **/

Collection<Sensor> getSensors();


}       // end of interface World




/* end of World.java */

