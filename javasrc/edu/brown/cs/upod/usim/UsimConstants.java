/********************************************************************************/
/*										*/
/*		UsimConstants.java						*/
/*										*/
/*	Constants for simulation package					*/
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



package edu.brown.cs.upod.usim;



public interface UsimConstants
{

	

/********************************************************************************/
/*										*/
/*	Time units								*/
/*										*/
/********************************************************************************/

long DEFAULT_TIME_UNIT = 60*1000;		// minutes
double DEFAULT_TIME_RATE = 60.0;		// one minute per second



/********************************************************************************/
/*										*/
/*	Type values								*/
/*										*/
/********************************************************************************/

enum PropertyType {
   STRING,
   BOOLEAN,
   INTEGER,
   REAL,
   TIME,
   DATE,
   DATETIME,
   SET, 		// set of values from enum or set of strings
   PICTURE,		// svg image or equivalent
}



/********************************************************************************/
/*										*/
/*	Geometry classes							*/
/*										*/
/********************************************************************************/

class Bounds {

   private double min_value;
   private double max_value;

   public Bounds() {
      this(0,0);
    }

   public Bounds(double min,double max) {
      min_value = Math.min(min,max);
      max_value = Math.max(min,max);
    }

   public double getMinValue() {
      return min_value;
    }

   public double getMaxValue() {
      return max_value;
    }

   public double getSpan()		{ return max_value - min_value; }
   
   public double getCenter()            { return (max_value + min_value)/2; }

   public void set(Bounds b) {
      min_value = b.min_value;
      max_value = b.max_value;
    }

   public void add(Bounds b) {
      min_value = Math.min(min_value,b.min_value);
      max_value = Math.max(max_value,b.max_value);
    }
   
   public void translate(double x) {
      min_value += x;
      max_value += x;
    }
   
   @Override public String toString() {
      return "[" + min_value + ":" + max_value + "]";
    }

}	// end of inner class Bounds




}	// end of interface UsimConstants




/* end of UsimConstants.java */

