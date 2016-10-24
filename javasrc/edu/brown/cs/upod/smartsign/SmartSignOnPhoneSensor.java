/********************************************************************************/
/*										*/
/*		SmartSignOnPhoneSensor.java					*/
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



public class SmartSignOnPhoneSensor extends BasisDevice
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static enum States { NOT_ON_PHONE, ON_PHONE };

private static final String	PHONE_NAME = "OnPhone";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartSignOnPhoneSensor(UpodUniverse su)
{
   super(su);
   initialize();
}

public SmartSignOnPhoneSensor(UpodUniverse uu,Element xml)
{
   super(uu,xml);
   initialize();
}


private void initialize()
{
   BasisParameter bp = BasisParameter.createEnumParameter(getUID(),States.ON_PHONE);
   bp.setIsSensor(true);
   bp.setLabel("On or Off Phone");
   UpodParameter pp = addParameter(bp);
   addConditions(pp);
   UpodCondition c1 = getCondition(pp,States.ON_PHONE);
   c1.setLabel("On the Phone");
   c1 = getCondition(pp,States.NOT_ON_PHONE);
   c1.setLabel("Not on the Phone");
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName() {
   return for_universe.getName() + NSEP + PHONE_NAME;
}

@Override public String getLabel()	{ return "On or Off the Phone"; }

@Override public String getDescription()
{
   return "Sensor to detect if one is on the phone (land-line) or not.";
}




}	// end of class SmartSignOnPhoneSensor




/* end of SmartSignOnPhoneSensor.java */

