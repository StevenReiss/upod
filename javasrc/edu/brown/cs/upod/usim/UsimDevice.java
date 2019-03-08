/********************************************************************************/
/*                                                                              */
/*              UsimDevice.java                                                 */
/*                                                                              */
/*      Device within the simulatiohn                                           */
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



package edu.brown.cs.upod.usim;


import java.util.*;


public interface UsimDevice extends UsimObject
{


List<UsimCommand> getCommands();
List<UsimParameter> getParameters();

UsimParameter getParameter(String name);
UsimCommand getCommand(String name);

Object getParameterValue(UsimParameter parm);
String getStringValue(UsimParameter parm);
int getIntValue(UsimParameter parm);
boolean getBooleanValue(UsimParameter parm);
double getDoubleValue(UsimParameter parm);
<T extends Enum<?>> T getEnumValue(UsimParameter parm,T dflt);

void applyCommand(UsimCommand cmd,Object... vals) throws UsimException;

void setValue(UsimParameter p,Object v);

}       // end of interface UsimDevice




/* end of UsimDevice.java */

