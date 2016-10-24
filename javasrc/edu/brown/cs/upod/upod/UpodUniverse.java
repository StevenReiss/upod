/********************************************************************************/
/*										*/
/*		UpodUniverse.java						*/
/*										*/
/*	Universe defines the problem domain					*/
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

import edu.brown.cs.ivy.xml.*;

import java.io.File;
import java.util.*;

public interface UpodUniverse
{

/**
 *	Return the identity of this universe.  This is used as a key
 *	when storing programs for users.
 **/

String getIdentity();


/**
 *	Return the external name for this universe
 **/
String getName();


/**
 *	Return the external label for this universe
 **/
String getLabel();



/**
 *	Return the set of available devices that can be acted upon.
 **/

Collection<UpodDevice> getDevices();

UpodDevice findDevice(String id);








/**
 *	Return the set of conditions to be presented to the user
 **/

Collection<UpodCondition> getBasicConditions();


/**
 *      Find basic condition given name
 **/

UpodCondition findBasicCondition(String name);


/**
 *      Find capability by name
 **/

UpodCapability findCapability(String id);



/**
 *	Return the set of hubs
 **/

Collection<UpodHub> getHubs();


/**
 *      Return the web server port number
 **/

int getWebServerPort();


/**
 *      Authorize a user for this universe
 **/

boolean authorize(String user,String sid,String userkey);

UpodAccess.Role getRole(String user);



/**
 *	Add an event listener for the universe
 **/

void addUniverseListener(UpodUniverse.Listener l);


/**
 *	Remove an event listener
 **/

void removeUniverseListener(UpodUniverse.Listener l);


/**
 *      Output the universe in its current state to read back in
 **/

void outputXml(IvyXmlWriter xw);


/**
 *      Return directory for this universe's data
 **/

File getBaseDirectory();



/**
 *      Start the universe running
 **/

void start();


/********************************************************************************/
/*										*/
/*	Interface for handling universe events					*/
/*										*/
/********************************************************************************/

interface Listener extends EventListener {
   void deviceAdded(UpodUniverse uu,UpodDevice ue);
   void deviceRemoved(UpodUniverse uu,UpodDevice ue);
   void conditionAdded(UpodUniverse uu,UpodCondition uc);
   void conditionRemoved(UpodUniverse uu,UpodCondition uc);
}	// end of inner interface Listener








}	// end of interface UpodUniverse




/* end of UpodUniverse.java */

