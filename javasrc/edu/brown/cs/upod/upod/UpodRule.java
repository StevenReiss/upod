/********************************************************************************/
/*										*/
/*		UpodRule.java							*/
/*										*/
/*	Rule definitions for user-programming of devices			*/
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






/**
 *	A rule consists of a condition implying an action.   Rules have priorities
 *	which serve to disambiguate conflicting rules.
 **/

public interface UpodRule extends UpodDescribable {



/**
 *	Return the condition associated with a rule
 **/

UpodCondition getCondition();



/**
 *	Return the action associated with a rule
 **/

UpodAction getAction();




/**
 *	Return the priority associated with the rule.  Priorities range from 0
 *	to 100 with 0 being the lowest and 100 the highest.  Typically priorities
 *	lower than 20 or higher than 80 will not be used (except for true
 *	background or emergency events).  Higher priority events will override
 *	lower priority ones that have overlapping actions.  Priorities less than
 *	10 should be used for manual override rules.
 **/

double getPriority();


/**
 *	Set the priority associated with this rule.
 **/

void setPriority(double p);







/**
 *	Apply a rule and wait for any action.  This returns false if the rule is
 *	not applicable (i.e. the condition doesn't hold). It can throw either
 *	ConditionException or ActionException if there are errors.  The rule
 *	is applied to the given world which may be the current one.
 **/

boolean apply(UpodWorld state) throws UpodConditionException, UpodActionException;



/**
 *	Apply a rule without waiting for the action to complete.
 **/

boolean applyAsync(UpodWorld state,UpodStatusHandler status) 
        throws UpodConditionException, UpodActionException;



}	// end of interface UpodRule




/* end of UpodRule.java */
