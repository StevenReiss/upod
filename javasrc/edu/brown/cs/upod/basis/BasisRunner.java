/********************************************************************************/
/*                                                                              */
/*              BasisRunner.java                                                */
/*                                                                              */
/*      Run a program using polling                                             */
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



package edu.brown.cs.upod.basis;

import edu.brown.cs.upod.upod.*;

import java.util.*;


public class BasisRunner implements BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private UpodProgram     run_program;
private UpodWorld       current_world;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisRunner(UpodProgram pgm,UpodWorld world)
{
   run_program = pgm;
   current_world = world;
}



/********************************************************************************/
/*                                                                              */
/*      Methods to run the world once                                           */
/*                                                                              */
/********************************************************************************/

public void runOnce()
{
   for (UpodRule ur : run_program.getRules()) {
      if (ur.apply(current_world)) break;
    }
}



/********************************************************************************/
/*                                                                              */
/*      Methods to run continuously                                             */
/*                                                                              */
/********************************************************************************/

public void runEvery(long delay)
{
   Timer t = new Timer("BasisRunner");
   t.schedule(new Runner(),0,delay);
}


private class Runner extends TimerTask {
   
   @Override public void run() {
      runOnce();
    }
   
}       // end of inner class Runner



}       // end of class BasisRunner




/* end of BasisRunner.java */

