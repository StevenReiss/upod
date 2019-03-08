/********************************************************************************/
/*                                                                              */
/*              SimDevice.java                                                  */
/*                                                                              */
/*      description of class                                                    */
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

import edu.brown.cs.upod.usim.*;

import org.w3c.dom.*;

import java.util.*;

class SimDevice extends SimObject implements UsimDevice
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SimSimulator              sim_master;
private Map<String,SimCommand>    active_commands;
private Map<String,SimParameter>  active_parameters;

private Map<UsimParameter,Object> param_values;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimDevice(SimSimulator ss,String name,String desc)
{
   super(name,desc);
   
   initialize(ss);
}


SimDevice(SimSimulator ss,Element xml)
{
   super(xml);
   
   initialize(ss);
}


private void initialize(SimSimulator ss)
{
   sim_master = ss;
   active_commands = new LinkedHashMap<String,SimCommand>();
   active_parameters = new LinkedHashMap<String,SimParameter>();
   param_values = new HashMap<UsimParameter,Object>();
   
   if (this instanceof UsimTimeHandler) {
      UsimTimeHandler th = (UsimTimeHandler) this;
      sim_master.getTime().addTimeHandler(th);
    }  
}



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

protected synchronized void addCommand(SimCommand cmd)
{
   active_commands.put(cmd.getName(),cmd);
}


protected synchronized void addParameter(SimParameter p,Object val)
{
   param_values.put(p,val);
   active_parameters.put(p.getName(),p);
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public List<UsimCommand> getCommands()
{
   return new ArrayList<UsimCommand>(active_commands.values());
}

@Override public List<UsimParameter> getParameters()
{
   return new ArrayList<UsimParameter>(active_parameters.values());
}



/********************************************************************************/
/*                                                                              */
/*      Parameter methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public UsimParameter getParameter(String nm)
{
   return active_parameters.get(nm);
}

@Override public Object getParameterValue(UsimParameter p)
{
   return param_values.get(p);
}


@Override public String getStringValue(UsimParameter p)
{
   Object o = getParameterValue(p);
   if (o == null) return null;
   return o.toString();
}


@Override public int getIntValue(UsimParameter p)
{
   Object o = getParameterValue(p);
   if (o == null) return 0;
   if (o instanceof Number) {
      Number n = (Number) o;
      return n.intValue();
    }
   return 0;
}


@Override public double getDoubleValue(UsimParameter p)
{
   Object o = getParameterValue(p);
   if (o == null) return 0;
   if (o instanceof Number) {
      Number n = (Number) o;
      return n.doubleValue();
    }
   return 0;
}


@Override public boolean getBooleanValue(UsimParameter p)
{
   Object o = getParameterValue(p);
   if (o == null) return false;
   if (o instanceof Boolean) {
      Boolean n = (Boolean) o;
      return n.booleanValue();
    }
   return false;
}

@SuppressWarnings("unchecked")
@Override public <T extends Enum<?>> T getEnumValue(UsimParameter p,T dflt)
{
   Object o = getParameterValue(p);
   if (o == null) return dflt;
   if (o.getClass() == dflt.getClass()) {
      return (T) o;
    }
   return dflt;
}



@Override public void setValue(UsimParameter p,Object v)
{
   SimParameter sp = active_parameters.get(p.getName());
   if (sp == null || sp != p) return;
   UsimType typ = sp.getType();
   v = typ.normalize(v);
   param_values.put(sp,v);
   sim_master.noteValueChange(this,sp,v);
}



/********************************************************************************/
/*                                                                              */
/*      Command methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public UsimCommand getCommand(String s)
{
   return active_commands.get(s);
}


@Override public void applyCommand(UsimCommand c,Object ... vals) throws UsimException
{
   if (c == null) throw new UsimException("No command");
   SimCommand cc = active_commands.get(c.getName());
   if (cc != c) throw new UsimException("Command not valid for device");
   
   cc.execute(this,vals);
}






}       // end of class SimDevice




/* end of SimDevice.java */

