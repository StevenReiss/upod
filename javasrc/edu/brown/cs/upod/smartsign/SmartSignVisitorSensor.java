/********************************************************************************/
/*										*/
/*		SmartSignVisitorSensor.java					*/
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



package edu.brown.cs.upod.smartsign;

import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;

import org.w3c.dom.Element;


public class SmartSignVisitorSensor extends BasisDevice {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static enum States { NO_VISITOR, WITH_VISITOR };

private static final String	SENSOR_NAME = "Visitor";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartSignVisitorSensor(UpodUniverse su)
{
   super(su);
   initialize();
}


public SmartSignVisitorSensor(UpodUniverse su,Element xml)
{
   super(su,xml);
   initialize();
}


private void initialize()
{
   BasisParameter bp = BasisParameter.createEnumParameter(getUID(),States.NO_VISITOR);
   bp.setIsSensor(true);
   bp.setLabel("Visitor Status");
   UpodParameter pp = addParameter(bp);
   addConditions(pp);
   UpodCondition c1 = getCondition(pp,States.WITH_VISITOR);
   c1.setLabel("With a Visitor");
   c1 = getCondition(pp,States.NO_VISITOR);
   c1.setLabel("Not with a Visitor");
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName() {
   return for_universe.getName() + NSEP + SENSOR_NAME;
}


@Override public String getLabel()
{
   return "With a Visitor or Not";
}

@Override public String getDescription()
{
   return "Sensor to detect if one has a visitor or not";
}






}	// end of class SmartSignVisitorSensor




/* end of SmartSignVisitorSensor.java */

