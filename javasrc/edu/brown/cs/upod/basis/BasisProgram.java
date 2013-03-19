/********************************************************************************/
/*                                                                              */
/*              BasisProgram.java                                               */
/*                                                                              */
/*      Basic implementation of a program                                       */
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


public class BasisProgram implements UpodProgram, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String                  program_name;
private Queue<UpodRule>         rule_list;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisProgram(String name)
{
   program_name = name;
   rule_list = new PriorityQueue<UpodRule>(10,new RuleComparator());
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()                       { return program_name; }



@Override public List<UpodRule> getRules()
{
   return new ArrayList<UpodRule>(rule_list);
}



/********************************************************************************/
/*                                                                              */
/*      Program run methods                                                     */
/*                                                                              */
/********************************************************************************/

@Override public boolean runOnce(UpodWorld w)
{
   for (UpodRule r : rule_list) {
      try {
         if (r.apply(w)) return true;
       }
      catch (UpodException e) {
         System.err.println("BASIS: Problem with rule " + r.getName() + ": " + e);
         e.printStackTrace();
       }
    }
   
   return false;
}

/********************************************************************************/
/*                                                                              */
/*      Rule priority comparator                                                */
/*                                                                              */
/********************************************************************************/

private static class RuleComparator implements Comparator<UpodRule> {
   
   @Override public int compare(UpodRule r1,UpodRule r2) {
      double v = r1.getPriority() - r2.getPriority();
      if (v > 0) return -1;
      if (v < 0) return 1;
      return r1.getName().compareTo(r2.getName());
    }
   
}       // end of inner class RuleComparator



}       // end of class BasisProgram




/* end of BasisProgram.java */

