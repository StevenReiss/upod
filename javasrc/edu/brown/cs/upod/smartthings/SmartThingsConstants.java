/********************************************************************************/
/*                                                                              */
/*              SmartThingsConstants.java                                       */
/*                                                                              */
/*      Constants for interface to SmartThings hub                              */
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



package edu.brown.cs.upod.smartthings;

import edu.brown.cs.upod.basis.BasisConstants;



public interface SmartThingsConstants extends BasisConstants
{

String SMART_THINGS_NAME = "SmarterThings";

String SMART_THINGS_UNIVERSE = "/ws/volfred/smartthings/smartthings.universe.xml";
String SMART_THINGS_PROGRAM = "$(HOME)/.smartthings";


/********************************************************************************/
/*                                                                              */
/*      Communications constants                                                */
/*                                                                              */
/********************************************************************************/

String SMART_THINGS_DATA = "/ws/volfred/smartthings/smartthings.json";



}       // end of interface SmartThingsConstants




/* end of SmartThingsConstants.java */

