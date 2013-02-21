/********************************************************************************/
/*                                                                              */
/*              ActionConflict.java                                             */
/*                                                                              */
/*      Represent a conflict between two actions or conditions                  */
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


/**
 *      The user interface will want to check if a new action or condition
 *      conflicts with an existing action in order to warn the user.  An 
 *      instance of this interface is returned for such a comparion showing 
 *      if there is a conflict and describing it if so.
 **/


public interface Conflict extends Describable
{

/**
 *      types of conflict
 **/

enum Type {
   NO_CONFLICT,
   CONTRADICTION,
   IMPLIES,
   IMPLIED_BY,
   IMPLIES_NOT,
   NOT_IMPLIED_BY,
   SAME,
   USUALLY_IMPLIES,
   USUALLY_IMPLIED_BY,
   USUALLY_SAME,
   EVENTUALLY_IMPLIES,
   EVENTUALLY_IMPLIED_BY,
   EVENTUALLY_SAME
};




/**
 *      Return the conflict type
 **/

Type getConflictType();



}       // end of interface Conflict




/* end of Conflict.java */

