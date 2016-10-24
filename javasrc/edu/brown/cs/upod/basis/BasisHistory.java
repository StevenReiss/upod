/********************************************************************************/
/*										*/
/*		BasisHistory.java						*/
/*										*/
/*	Record history for use in rule deduction				*/
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



package edu.brown.cs.upod.basis;

import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.xml.*;

import java.io.*;
import java.util.Date;
import java.util.TimerTask;


public class BasisHistory implements BasisConstants, UpodDeviceHandler,
	UpodConditionHandler
{

/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private IvyXmlWriter	history_file;
private UpodUniverse	for_universe;
private UpodWorld	the_world;

private static boolean  record_conditions = false;

private static final long	SAVE_EVERY = 5*T_MINUTE;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisHistory(UpodUniverse uu)
{
   for_universe = uu;
   the_world = BasisFactory.getFactory().getCurrentWorld(uu);
   File f1 = for_universe.getBaseDirectory();
   File f = new File(f1,HISTORY_FILE + "." + uu.getName() + ".xml");
   try {
      FileWriter fw = new FileWriter(f,true);
      history_file = new IvyXmlWriter(fw);
    }
   catch (IOException e) {
      BasisLogger.logE("Can't open history file " + f);
      history_file = null;
    }

   for (UpodDevice ue : uu.getDevices()) {
      ue.addDeviceHandler(this);
    }
   for (UpodCondition uc : uu.getBasicConditions()) {
      uc.addConditionHandler(this);
    }

   recordStart();

   BasisWorld.getWorldTimer().schedule(new SaveTask(),SAVE_EVERY,SAVE_EVERY);

   Runtime.getRuntime().addShutdownHook(new FinishTask());
}


/********************************************************************************/
/*										*/
/*	Callback handlers							*/
/*										*/
/********************************************************************************/

@Override public void stateChanged(UpodWorld w,UpodDevice e)
{
   if (!w.isCurrent()) return;
   recordDevice(e);
}

@Override public void conditionOn(UpodWorld w,UpodCondition c,UpodPropertySet p)
{
   if (!w.isCurrent()) return;
   recordCondition(c,p,null);
}

@Override public void conditionTrigger(UpodWorld w,UpodCondition c,UpodPropertySet p)
{
   if (!w.isCurrent()) return;
   recordCondition(c,p,null);
}

@Override public void conditionOff(UpodWorld w,UpodCondition c)
{
   if (!w.isCurrent()) return;
   recordCondition(c,null,null);
}

@Override public void conditionError(UpodWorld w,UpodCondition c,Throwable cause)
{
   if (!w.isCurrent()) return;
   recordCondition(c,null,cause);
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

private synchronized void recordStart()
{
   if (history_file == null) return;
   history_file.begin("START");
   history_file.field("WHEN",System.currentTimeMillis());
   UpodWorld uw = BasisFactory.getFactory().getCurrentWorld(for_universe);
   uw.outputXml(history_file);
   for (UpodDevice ue : for_universe.getDevices()) {
      recordDevice(ue);
    }
   history_file.end("START");
}

private synchronized void recordDevice(UpodDevice e)
{
   if (history_file == null) return;
   history_file.begin("CHANGE");
   history_file.field("WHEN",System.currentTimeMillis());
   history_file.field("DEVICE",e.getUID());
   history_file.field("NAME",e.getName());

   for (UpodParameter up : e.getParameters()) {
      if (up.isSensor()) {
	 Object st = e.getValueInWorld(up,the_world);
	 if (st != null) {
	    history_file.begin("PARAM");
	    history_file.field("NAME",up.getName());
	    history_file.cdata(st.toString());
	    history_file.end("PARAM");
	  }
       }
    }
   history_file.end("CHANGE");
}



private synchronized void recordCondition(UpodCondition c,UpodPropertySet p,Throwable t)
{
   if (history_file == null) return;
   if (!record_conditions) return;
   
   history_file.begin("CHANGE");
   history_file.field("WHEN",System.currentTimeMillis());
   history_file.field("DATE",new Date().toString());
   history_file.field("CONDITION",c.getName());
   if (p == null && t == null) {
      history_file.field("SET","FALSE");
    }
   else if (t != null) {
      history_file.field("SET","ERROR");
      history_file.textElement("ERROR",t.toString());
    }
   else if (p != null) {
      history_file.field("SET","TRUE");
      for (String up : p.keySet()) {
	 history_file.begin("PROPERTY");
	 history_file.field("NAME",up);
	 history_file.field("VALUE",p.get(up).toString());
	 history_file.end("PROPERTY");
       }
    }
   history_file.end("CHANGE");
}


private synchronized void flush()
{
   if (history_file == null) return;
   history_file.flush();
}


private synchronized void close()
{
   if (history_file == null) return;
   history_file.close();
   history_file = null;
}


/********************************************************************************/
/*										*/
/*	Tasks to ensure history file is saved					*/
/*										*/
/********************************************************************************/

private class SaveTask extends TimerTask {

   @Override public void run() {
      flush();
    }

}	// end of inner class SaveTask


private class FinishTask extends Thread {

   @Override public void run() {
      close();
    }

}	// end of inner class FinishTask



}	// end of class BasisHistory




/* end of BasisHistory.java */

