/********************************************************************************/
/*										*/
/*		UmonControl.java						*/
/*										*/
/*	Controller for upod monitor						*/
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

import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;

import edu.brown.cs.ivy.xml.*;
import edu.brown.cs.ivy.file.*;

import org.w3c.dom.*;
import java.io.*;
import java.util.*;

public class UmonControl implements UmonConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private UpodProgram		the_program;
private UpodUniverse		the_universe;
private UpodWorld		current_world;
private File			save_file;

private static boolean		read_stdin = false;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

UmonControl(UpodUniverse uu,UpodProgram pgm,String file)
{
   the_program = pgm;
   the_universe = uu;
   save_file = new File(file);
}


public UmonControl(UpodUniverse uu,String file)
{
   the_program = null;
   the_universe = uu;
   save_file = new File(file);
   loadProgram(file);
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

public void process()
{
   current_world = BasisFactory.getFactory().getCurrentWorld(the_universe);
   new BasisHistory(the_universe);

   BasisWorld.getWorldTimer().schedule(new RunOnce(),5000L);
   // the_program.runOnce(current_world,null);

   new UmonWebServer(this,0);

   if (read_stdin) {
      @SuppressWarnings("resource") IvyXmlReader xr = new IvyXmlReader(System.in);
      try {
	 for ( ; ; ) {
	    String in = xr.readXml();
	    if (in != null && in.length() > 0) break;
	    // process XML command <in>
	  }
       }
      catch (IOException e) { }
    }
   else {
      for ( ; ; ) {
         synchronized (this) {
            try { 
               wait();
             }
            catch (InterruptedException e) { } 
          }
       }
    }
}


void stop() {
   System.exit(0);
}


private class RunOnce extends TimerTask {
   
   @Override public void run() {
      the_program.runOnce(current_world,null);
    }
   
}

/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

UpodUniverse getUniverse()		{ return the_universe; }

UpodProgram getProgram()		{ return the_program; }




/********************************************************************************/
/*										*/
/*	Methods to load/save a program from saved xml file			*/
/*										*/
/********************************************************************************/

private void loadProgram(String file)
{
   Element xml = IvyXml.loadXmlFromFile(file);
   the_program = new BasisProgram(the_universe,xml);
   if (xml == null) saveProgram();
}


void saveProgram()
{
   try {
      File f1 = new File(save_file.getParentFile(),save_file.getName() + ".new");
      File f2 = new File(save_file.getParentFile(),save_file.getName() + ".save");
      IvyXmlWriter xw = new IvyXmlWriter(f1);
      the_program.outputXml(xw);
      xw.close();
      if (save_file.exists()) {
         IvyFile.copyFile(save_file,f2);
       }
      f1.renameTo(save_file);
    }
   catch (IOException e) {
      BasisLogger.log("Problem saving program: " + e);
    }
}

}	// end of class UmonControl




/* end of UmonControl.java */

