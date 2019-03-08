/********************************************************************************/
/*										*/
/*		SmartHabConstants.java						*/
/*										*/
/*	Constants for talking to the smarthab server				*/
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



package edu.brown.cs.upod.smarthab;

import edu.brown.cs.upod.basis.BasisConstants;
import java.util.*;
import java.awt.geom.*;


public interface SmartHabConstants extends BasisConstants
{


/********************************************************************************/
/*										*/
/*	Standard names								*/
/*										*/
/********************************************************************************/

String SMART_HAB_NAME = "SmartHab";

String SMART_HAB_UNIVERSE = "/ws/volfred/smarthab/smarthab.universe.xml";
String SMART_HAB_PROGRAM = "$(HOME)/.smarthab";



/********************************************************************************/
/*										*/
/*	OpenHab communications constants					*/
/*										*/
/********************************************************************************/

String	DEFAULT_HOST = "fred4.cs.brown.edu";
int	DEFAULT_PORT = 8080;
String	DEFAULT_USER = null;




/********************************************************************************/
/*										*/
/*	Widget data for creating capabilities					*/
/*										*/
/********************************************************************************/

class WidgetData {

   String widget_type;
   Double range_min;
   Double range_max;
   Map<String,String> value_map;

   WidgetData(String type) {
      widget_type = type;
      range_min = null;
      range_max = null;
      value_map = null;
    }

   void setRange(double min,double max) {
      range_min = min;
      range_max = max;
    }

   void addMapping(String frm,String to) {
      if (value_map == null) value_map = new HashMap<String,String>();
      value_map.put(frm,to);
    }

   void mergeWith(WidgetData wd) {
      if (wd.range_min != null) {
	 if (range_min == null) {
	    range_min = wd.range_min;
	    range_max = wd.range_max;
	  }
	 else {
	    range_min = Math.min(range_min,wd.range_min);
	    range_max = Math.max(range_max,wd.range_max);
	  }
       }
      if (wd.value_map != null) {
	 if (value_map == null) value_map = wd.value_map;
	 else value_map.putAll(wd.value_map);
       }
    }

   String getType()                             { return widget_type; }
   
   Point2D getRange() {
      if (range_min == null) return null;
      return new Point2D.Double(range_min,range_max);
    }
   
   Map<String,String> getMapping ()             { return value_map; }

}	// end of inner class WidgetData



}	// end of interface SmartHabConstants




/* end of SmartHabConstants.java */

