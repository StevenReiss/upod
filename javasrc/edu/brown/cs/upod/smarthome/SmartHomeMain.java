/********************************************************************************/
/*                                                                              */
/*              SmartHomeMain.java                                              */
/*                                                                              */
/*      Main program for controlling the smart home simulation                  */
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



package edu.brown.cs.upod.smarthome;

import edu.brown.cs.upod.umon.*;
import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;

import edu.brown.cs.ivy.file.*;

import java.io.*;

public class SmartHomeMain implements SmartHomeConstants
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
   
   File file = new File(SMART_HOME_UNIVERSE);
   if (docreate) file.delete();
   UpodUniverse ssu = BasisUniverse.createUniverse(null,SmartHomeUniverse.class);
   String fnm = IvyFile.expandName(SMART_HOME_PROGRAM);
   
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
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/



/********************************************************************************/
/*                                                                              */
/*      Default program creation                                                */
/*                                                                              */
/********************************************************************************/

private static void createProgram(UpodUniverse uu,String filename)
{ }



}       // end of class SmartHomeMain




/* end of SmartHomeMain.java */

