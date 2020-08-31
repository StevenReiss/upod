/********************************************************************************/
/*										*/
/*		SmartSignHomePresenceSensor.java				*/
/*										*/
/*	Detect if working at home						*/
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



package edu.brown.cs.upod.smartsign;


import org.w3c.dom.Element;

import edu.brown.cs.upod.basis.BasisDevice;
import edu.brown.cs.upod.basis.BasisParameter;
import edu.brown.cs.upod.upod.UpodCondition;
import edu.brown.cs.upod.upod.UpodParameter;
import edu.brown.cs.upod.upod.UpodUniverse;

public class SmartSignHomePresenceSensor extends BasisDevice implements SmartSignConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private UpodParameter	 presence_param;

private static final String	SENSOR_NAME = "HomePresenceSensor";

static enum State { WORKING, IDLE, AWAY, NOTHOME };



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartSignHomePresenceSensor(UpodUniverse u)
{
   super(u);
   initialize();
}


public SmartSignHomePresenceSensor(UpodUniverse u,Element xml)
{
   super(u,xml);
   initialize();
}


private void initialize()
{
   BasisParameter pp = BasisParameter.createEnumParameter(getUID(),State.NOTHOME);
   pp.setLabel("HomePresenceSensor");
   pp.setIsSensor(true);
   presence_param = addParameter(pp);
   addConditions(presence_param);
   UpodCondition c1 = getCondition(presence_param,State.WORKING);
   c1.setLabel("Working At Home");
   c1 = getCondition(presence_param,State.IDLE);
   c1.setLabel("Working At Home, Stepped Out");
   c1 = getCondition(presence_param,State.AWAY);
   c1.setLabel("Working At Home, Out of Office");
   c1 = getCondition(presence_param,State.NOTHOME);
   c1.setLabel("Not At Home");
}



/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   return for_universe.getName() + NSEP + SENSOR_NAME;
}

@Override public String getLabel()
{
   return "Working at Home";
}

@Override public String getDescription()
{
   return "Sensor to detect if one is working at home";
}



}	// end of class SmartSignHomePresenceSensor




/* end of SmartSignHomePresenceSensor.java */

