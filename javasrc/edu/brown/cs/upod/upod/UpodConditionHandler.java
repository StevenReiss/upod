/********************************************************************************/
/*										*/
/*		UpodConditionHandler.java					*/
/*										*/
/*	description of class							*/
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

import java.util.EventListener;


public interface UpodConditionHandler extends EventListener
{

/**
 *	Invoked when a condition turns on.  The parameter set passed in
 *	may be null; if not it contains values describing the condition.
 **/

void conditionOn(UpodWorld w,UpodCondition c,UpodParameterSet p);


/**
 *	Invoked when a condition turns off
 **/

void conditionOff(UpodWorld w,UpodCondition c);



/**
 *	Invoked for a trigger condition (rather than on-off).  The
 *	parameter set contains values describing the condition.  It
 *	might be null.
 **/

void conditionTrigger(UpodWorld w,UpodCondition c,UpodParameterSet p);



/**
 *	Handle errors in checking the condition
 **/

void conditionError(UpodWorld w,UpodCondition c,Throwable cause);


}	// end of interface UpodConditionHandler




/* end of UpodConditionHandler.java */

