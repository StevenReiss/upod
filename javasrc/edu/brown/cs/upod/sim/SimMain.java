/********************************************************************************/
/*                                                                              */
/*              SimMain.java                                                    */
/*                                                                              */
/*      Main program for running a simulation                                   */
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



package edu.brown.cs.upod.sim;

import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.*;

import java.util.*;
import java.text.*;
import java.io.*;


public class SimMain implements SimConstants
{


/********************************************************************************/
/*                                                                              */
/*      Main Program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   SimMain sm = new SimMain(args);
   
   sm.start();
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SimSimulator    the_simulator;

private static DateFormat date_format = new SimpleDateFormat();



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private SimMain(String [] args)
{
   the_simulator = new SimSimulator();
   
   scanArgs(args);
}



/********************************************************************************/
/*                                                                              */
/*      Argument processing methods                                             */
/*                                                                              */
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
         if (args[i].startsWith("-r") && i+1 < args.length) {           // -r rate
            try {
               double d = Double.parseDouble(args[++i]);
               the_simulator.getTime().setRate(d);
             }
            catch (NumberFormatException e) { badArgs(); }
          }
         else if (args[i].startsWith("-u") && i+1 < args.length) {      // -u unit (in ms)
            try {
               long u = Long.parseLong(args[++i]);
               the_simulator.getTime().setUnit(u);
             }
            catch (NumberFormatException e) { badArgs(); }
          }
         else if (args[i].startsWith("-t") && i+1 < args.length) {      // -t time
            try {
               Date d = date_format.parse(args[++i]);
               Calendar c = Calendar.getInstance();
               c.setTime(d);
               the_simulator.getTime().setTime(c);
             }
            catch (ParseException e) { badArgs(); }
          }
         else badArgs();
       }
      else {
         try {
            loadDataFile(args[i]);
          }
         catch (IOException e) { badArgs(); }
       }
    }
}



private void badArgs()
{
   System.err.println("SIMMAIN: [-r rate][-u unit][-t date/time] datafile");
   System.exit(1);
}




/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                       */
/*                                                                              */
/********************************************************************************/

private void start()
{
   SimDisplay disp = new SimDisplay(the_simulator);
   disp.begin();
   
   the_simulator.getTime().start();
   
   SimObject obj = new SimAvatar("Avatar_1","spr@cs.brown.edu",
         new SimLocation(10,50));
   the_simulator.addObject(obj);
   disp.setControlledObject(obj);
}



/********************************************************************************/
/*                                                                              */
/*      Methods to load a data file                                             */
/*                                                                              */
/********************************************************************************/

private void loadDataFile(String nm) throws IOException
{
   Element root = IvyXml.loadXmlFromFile(nm);
   if (root == null) throw new IOException("Bad input file");
   
   Element house = root;
   if (!IvyXml.isElement(house,"HOUSE")) house = IvyXml.getElementByTag(root,"HOUSE");
   for (Element itm : IvyXml.children(house)) {
      SimObject obj = null;
      switch (itm.getNodeName()) {
         case "WALL" :
            obj = new SimWall(the_simulator,itm);
            break;
         case "WINDOW" :
            obj = new SimWindow(the_simulator,itm);
            break;
         case "DOOR" :
            obj = new SimDoor(the_simulator,itm);
            break;
         case "FLOOR" :
            obj = new SimFloor(the_simulator,itm);
            break;
         case "CEILING" :
            obj = new SimCeiling(the_simulator,itm);
            break;
         default :
            break;
       }
      if (obj != null) the_simulator.addObject(obj);
    }
}

}       // end of class SimMain




/* end of SimMain.java */

