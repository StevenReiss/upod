/********************************************************************************/
/*										*/
/*		SmartSignPresenceSensor.java					*/
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

import edu.brown.cs.ivy.exec.*;

import java.io.*;

import org.w3c.dom.Element;


public class SmartSignPresenceSensor extends BasisDevice implements SmartSignConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private UpodParameter	 presence_param;
private boolean 	 doing_update;

private static final long	RUN_EVERY = T_MINUTE;

private static final String	SENSOR_NAME = "Presence";

static enum State { IN, OUT };



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartSignPresenceSensor(UpodUniverse u)
{
   super(u);
   initialize();
}


public SmartSignPresenceSensor(UpodUniverse u,Element xml)
{
   super(u,xml);
   initialize();
}


private void initialize()
{
   doing_update = false;
   BasisParameter pp = BasisParameter.createEnumParameter(getUID(),State.OUT);
   pp.setLabel("PresenseSensor");
   pp.setIsSensor(true);
   presence_param = addParameter(pp);
   addConditions(presence_param);
   UpodCondition c1 = getCondition(presence_param,State.IN);
   c1.setLabel("In Office");
   c1 = getCondition(presence_param,State.OUT);
   c1.setLabel("Out of Office");
   BasisCapabilityPolled poller = new BasisCapabilityPolled("PresencePoller",RUN_EVERY);
   addCapability(poller);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   return for_universe.getName() + NSEP + SENSOR_NAME;
}

@Override public String getLabel()
{
   return "In or Out Of Office";
}

@Override public String getDescription()
{
   return "Sensor to detect if one is in the office or not";
}


/********************************************************************************/
/*										*/
/*	State checking methods							*/
/*										*/
/********************************************************************************/

@Override protected void updateCurrentState()
{
   UpodWorld cw = getCurrentWorld();

   synchronized (this) {
      if (doing_update) return;
      doing_update = true;
    }

   try {
      IvyExec ex = new IvyExec("checkphone");
      int sts = ex.waitFor();
      State state = (sts != 0 ? State.IN : State.OUT);
      // BasisLogger.logD("PRESENSE: " + state);
      setValueInWorld(presence_param,state,cw);
    }
   catch (IOException e) {
    }

   synchronized (this) {
      doing_update = false;
    }
}




}	// end of class SmartSignPresenceSensor




/* end of SmartSignPresenceSensor.java */

