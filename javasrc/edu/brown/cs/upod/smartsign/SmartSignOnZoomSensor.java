/********************************************************************************/
/*                                                                              */
/*              SmartSignOnZoomSensor.java                                      */
/*                                                                              */
/*      Sensor to detect if on zoom from home                                   */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.upod.smartsign;

import org.w3c.dom.Element;

import edu.brown.cs.upod.basis.BasisDevice;
import edu.brown.cs.upod.basis.BasisParameter;
import edu.brown.cs.upod.upod.UpodCondition;
import edu.brown.cs.upod.upod.UpodParameter;
import edu.brown.cs.upod.upod.UpodUniverse;

public class SmartSignOnZoomSensor extends BasisDevice implements SmartSignConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/


private static enum States { NOT_ON_ZOOM, ON_ZOOM };

private static final String	ZOOM_NAME = "OnZoom";



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/


public SmartSignOnZoomSensor(UpodUniverse su)
{
   super(su);
   initialize();
}

public SmartSignOnZoomSensor(UpodUniverse uu,Element xml)
{
   super(uu,xml);
   initialize();
}


private void initialize()
{
   BasisParameter bp = BasisParameter.createEnumParameter(getUID(),States.ON_ZOOM);
   bp.setIsSensor(true);
   bp.setLabel("In a Zoom Meeting");
   UpodParameter pp = addParameter(bp);
   addConditions(pp);
   UpodCondition c1 = getCondition(pp,States.ON_ZOOM);
   c1.setLabel("In a Zoom Meeting");
   c1 = getCondition(pp,States.NOT_ON_ZOOM);
   c1.setLabel("Not in a Zoom Meeting");
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName() {
   return for_universe.getName() + NSEP + ZOOM_NAME;
}

@Override public String getLabel()	{ return "In or Not In a Zoom Meeting"; }

@Override public String getDescription()
{
   return "Sensor to detect if one is currently in a Zoom meeting.";
}



}       // end of class SmartSignOnZoomSensor




/* end of SmartSignOnZoomSensor.java */

