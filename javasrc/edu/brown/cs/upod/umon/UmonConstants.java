/********************************************************************************/
/*										*/
/*		UmonConstants.java						*/
/*										*/
/*	Constants for UPOD monitor package					*/
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



package edu.brown.cs.upod.umon;

import edu.brown.cs.upod.basis.*;



public interface UmonConstants extends BasisConstants
{



/********************************************************************************/
/*										*/
/*	Commands								*/
/*										*/
/********************************************************************************/

enum CommandName {
   NONE,			// null command

   LIST,			// provide world description
   LIST_PROGRAM,		// list current rule set
   LIST_RESTRICT,               // list rules restricted by device or condition

   LIBRARY,			// return library for action selection

   ADD_RULE,			// create new rule
   CHANGE_RULE_PRIORITY,	// change priority of a rule
   REMOVE_RULE, 		// remove a rule

   NEW_SENSOR,			// add a sensor
   NEW_ENTITY,			// add an entity

   SET_SENSOR,			// set start of a sensor explicitly
   DO_ACTION,			// apply transition to an entity

   CREATE_WORLD,		// create hypothetical world
   REMOVE_WORLD,		// finish with a hypothetical world

   SET_TIME,			// set time in hypothetical world

   DEDUCE_RULE, 		// add information for rule deduction

   STOP,			// stop the server (exit)

   PING 			// liveness check		

}


enum DeviceCommands {

   WHORU,			// provide identity information
   UPDATE,			// update status

   PING 			// liveness check
}



/********************************************************************************/
/*										*/
/*	Socket Client Definitions						*/
/*										*/
/********************************************************************************/

int	WEB_PORT = 8800;

String	XML_MIME = "application/xml";
String	TEXT_MIME = "text/plain";
String	HTML_MIME = "text/html";
String	CSS_MIME = "text/css";
String	JS_MIME = "application/javascript";
String	SVG_MIME = "image/svg+xml";
String	PNG_MIME = "image/png";



/********************************************************************************/
/*										*/
/*	Session definitions							*/
/*										*/
/********************************************************************************/

long	SESSION_TIMEOUT = 4 * T_DAY;



/********************************************************************************/
/*										*/
/*	Web definitoins 							*/
/*										*/
/********************************************************************************/

String TEMPLATE_DIR = "/research/people/spr/upod/templates";
String HTML_DIR = "/research/people/spr/upod/web";

String STATUS_PAGE = "upodstatus.vel";

String HOME_PAGE = "home.vel";

String SESSION_COOKIE = "Upod.Session";
String SESSION_FILE = "session.xml";

}	// end of interface UmonConstants




/* end of UmonConstants.java */

