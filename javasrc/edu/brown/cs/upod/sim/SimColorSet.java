/********************************************************************************/
/*                                                                              */
/*              SimColorSet.java                                                */
/*                                                                              */
/*      Keep track of colors for an object                                      */
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
import edu.brown.cs.ivy.swing.*;

import org.w3c.dom.*;

import java.awt.*;
import java.util.*;


class SimColorSet implements SimConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private         Color   top_color;
private         Color   bottom_color;
private         Color [] side_colors;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimColorSet(Element xml)
{
   float alpha = (float)(1.0 - IvyXml.getAttrDouble(xml,"T",0));
   
   side_colors = null;
   String cols = IvyXml.getAttrString(xml,"COLOR");
   if (cols != null && alpha > 0) {
      StringTokenizer tok = new StringTokenizer(cols," ,;");
      side_colors = new Color[tok.countTokens()];
      int ct = 0;
      while (tok.hasMoreTokens()) {
         Color c = SwingColorSet.getColorByName(tok.nextToken());
         if (c == null) c = Color.WHITE;
         side_colors[ct++] = alphaColor(c,alpha);
       }
    } 
   else if (alpha > 0) { 
      side_colors = new Color [] { new Color(1f,1f,1f,alpha) };
    }
   
   Color c = side_colors[0];
   top_color = IvyXml.getAttrColor(xml,"TOPCOLOR",c);
   top_color = alphaColor(top_color,alpha);
   bottom_color = IvyXml.getAttrColor(xml,"BOTTOMCOLOR",c);
   bottom_color = alphaColor(bottom_color,alpha);
}



SimColorSet(Color ... c)
{
   side_colors = c;
   top_color = c[0];
   bottom_color = c[0];
}


SimColorSet()
{
   this(Color.WHITE);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

Color getTopColor()                     { return top_color; }
void setTopColor(Color c)               { top_color = c; }

Color getBottomColor()                  { return bottom_color; }
void setBottomColor(Color c)            { bottom_color = c; }



Color getSideColor(int idx)
{
   if (side_colors == null || side_colors.length == 0) return null;
   
   int index = idx % side_colors.length;
   
   return side_colors[index];
}



private Color alphaColor(Color c,float alpha)
{
   if (alpha == 1.0) return c;
   if (c == null) return null;
   if (alpha == 0) return null;
   
   float r = (c.getRed()/255f);
   float g = (c.getGreen()/255f);
   float b = (c.getBlue()/255f);
   float a = c.getAlpha();
   if (a != 255) return c;
   
   return new Color(r,g,b,alpha);
}




}       // end of class SimColorSet




/* end of SimColorSet.java */

