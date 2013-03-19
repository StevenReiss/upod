/********************************************************************************/
/*										*/
/*		UpodParameter.java						*/
/*										*/
/*	Description of a parameter that can be associated with an Action	*/
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



public interface UpodParameter extends UpodDescribable
{


/**
 *	Supported parameter types
 **/

enum ParameterType {
   STRING,
   BOOLEAN,
   INTEGER,
   REAL,
   TIME,
   DATE,
   DATETIME,
   TIME_RANGE,
   DATE_RANGE,
   DATETIME_RANGE,
   ENUM,
   EVENT_MATCH,         // calendar event matching
};



/**
 *	Return the type of the parameter
 **/

ParameterType getParameterType();



/**
 *	Range values.  These are used for both real and integer types.
 *	They are also used for dates/datetimes using time in millis.
 *	This function returns the minimum value.
 **/

double getMinValue();


/**
 *	Range values.  These are used for both real and integer types.
 *	They are also used for dates/datetimes using time in millis.
 *	This function returns the maximum value.
 **/

double getMaxValue();


/**
 *	Validate this parameter.  This returns false if the parameter is
 *	invalid.  Note that its returning true is no guarantee that the
 *	parameter is valid.
 **/

boolean isValid(Object value);


}	// end of interface UpodParameter




/* end of UpodParameter.java */

