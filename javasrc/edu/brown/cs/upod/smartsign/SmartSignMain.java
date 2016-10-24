/********************************************************************************/
/*                                                                              */
/*              SmartSignMain.java                                              */
/*                                                                              */
/*      Main program for smart sign                                             */
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

import edu.brown.cs.upod.umon.*;
import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;

import edu.brown.cs.ivy.file.*;
import edu.brown.cs.ivy.xml.*;

import java.util.*;
import java.io.*;

public class SmartSignMain implements SmartSignConstants
{




/********************************************************************************/
/*                                                                              */
/*      Main Program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   boolean docreate = false;
   if (args.length > 0 && args[0].equals("create")) {
      docreate = true;
    }
   
   File file = new File(SMART_SIGN_UNIVERSE);
   if (docreate) file.delete();
   UpodUniverse ssu = BasisUniverse.createUniverse(file,SmartSignUniverse.class);
   String fnm = IvyFile.expandName(SMART_SIGN_PROGRAM);
   
   if (ssu == null) return;
   
   if (docreate ) {
      createProgram(ssu,fnm);
      return;
    }
   
   UmonControl uc = new UmonControl(ssu,fnm);
   uc.process();
}



/********************************************************************************/
/*                                                                              */
/*      Test to create a sample program                                         */
/*                                                                              */
/********************************************************************************/

private static void createProgram(UpodUniverse uu,String filename)
{
   BasisProgram pgm = new BasisProgram(uu);
   UpodDevice us = uu.findDevice("Presence"); 
   UpodParameter up = getParameter(us);
   UpodCondition in = us.getCondition(up,SmartSignPresenceSensor.State.IN);
   UpodCondition out = us.getCondition(up,SmartSignPresenceSensor.State.OUT);
   UpodDevice ue = uu.findDevice("SignDisplay");
   UpodTransition ut = getTransition(ue,"SignChanger");
   
   UpodAction ua = new BasisAction(ue,ut);
   ua.setDescription("Say I'm Available, Please Knock");
   UpodParameterSet ps = ut.getDefaultParameters();
   UpodParameter file = ut.findParameter("SET" + NSEP + "Display");
   String svg = getFileContents("/vol/smartsign/inmessage.svg");
   ps.put(file,svg);
   ua.setParameters(ps); 
   UpodRule ur = new BasisRule(in,Collections.singleton(ua),null,50);
   ur.setDescription("Tell people I'm around");
   pgm.addRule(ur);
   
   ua = new BasisAction(ue,ut);
   ua.setDescription("Say I'm Out, Try Later");
   ps = ut.getDefaultParameters();
   svg = getFileContents("/vol/smartsign/outmessage.svg");  
   ps.put(file,svg);
   ua.setParameters(ps); 
   ur = new BasisRule(out,Collections.singleton(ua),null,60);
   ur.setDescription("Tell people I'm away");
   pgm.addRule(ur);  
   
   try {
      IvyXmlWriter xw = new IvyXmlWriter(filename);
      pgm.outputXml(xw);
      xw.close();
    }
   catch (IOException e) { 
      System.err.println("PROBLEM SAVING PROGRAM: " + e);
    }
}

private static UpodParameter getParameter(UpodDevice ud)
{
   UpodParameter p0 = null;
   
   for (UpodParameter up : ud.getParameters()) {
      if (up.getName().equals(ud.getUID())) return up;
      if (p0 == null) p0 = up;
    }
   return p0;
}

private static UpodTransition getTransition(UpodDevice ud,String nm)
{
   String nm1 = ud.getName() + NSEP + nm;
   
   for (UpodTransition ut : ud.getTransitions()) {
      if (nm == null) return ut;
      else if (ut.getName().equals(nm)) return ut;
      else if (ut.getName().equals(nm1)) return ut;
    }
   return null;
}



private static String getFileContents(String file)
{
   char [] buf = new char[4096];
   StringBuffer sbuf = new StringBuffer();
   try {
      FileReader fr = new FileReader(file);
      for ( ; ; ) {
         int ln = fr.read(buf);
         if (ln <= 0) break;
         sbuf.append(buf,0,ln);
       }
      fr.close();
    }
   catch (IOException e) {
      return null;
    }
     
   return sbuf.toString();
}



}       // end of class SmartSignMain




/* end of SmartSignMain.java */

