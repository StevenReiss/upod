/********************************************************************************/
/*										*/
/*		UsimObject.java 						*/
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



package edu.brown.cs.upod.usim;

import com.jogamp.opengl.*;
import java.awt.geom.*;



public interface UsimObject extends UsimConstants, UsimDescribable, UsimIdentifiable
{

boolean isMovable();

/**
 *      Check if an object (obj) can move to the given location
 *      without being blocked or affected by this object.  If
 *      the move is okay and nothing is affected, it returns null.
 *      If the move is okay, but the location has to be changed, 
 *      it returns the new location.  This can happen for example
 *      when going up/down stairs.  If the move is not valid (i.e.
 *      an attempt to move through a wall, it throws an exception.
 **/

UsimLocation checkMove(UsimObject obj,UsimLocation loc) throws UsimException;

void moveTo(UsimLocation loc) throws UsimException;

void draw(GL2 ctx);

Rectangle2D getXYBounds();

Bounds getZBounds();

UsimLocation getCameraLookAt();
UsimLocation getCameraPosition();
UsimLocation getCameraRight();
UsimLocation getCameraUp();

}	// end of interface UsimObject




/* end of UsimObject.java */

