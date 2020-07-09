/********************************************************************************/
/*										*/
/*		SmartSignWeatherCondSensor.java 				*/
/*										*/
/*	Sensor for getting current weather condition data			*/
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



package edu.brown.cs.upod.smartsign;

import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;

import edu.brown.cs.ivy.file.*;
import edu.brown.cs.ivy.xml.*;

import org.json.JSONObject;

import org.w3c.dom.Element;

import java.util.*;


public class SmartSignWeatherCondSensor extends BasisSensorWeb implements SmartSignConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	zip_code;

private static final String DEFAULT_ZIP = "02912";

/**
private static final String weather_url_old =
   "http://www.weather.com/weather/local/$(ZIP)?cm_ven=LWO&cm_cat=rss&cm_pla=city_page" +
   "&cm_ite=cc2&site=city_page";
****/

/**
private static final String weather_url =
   "http://www.wunderground.com/cgi-bin/findweather/getForecast?query=$(ZIP)";
*********/

private static final String weather_url =
   "http://api.openweathermap.org/data/2.5/weather/?zip=$(ZIP)&APPID=$(APPID)&units=imperial";


private static final String cond_select = "#curCond .wx-value";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartSignWeatherCondSensor(UpodUniverse uu)
{
   super(uu,weather_url,cond_select,T_MINUTE);

   zip_code = DEFAULT_ZIP;

   initialize();
}



public SmartSignWeatherCondSensor(UpodUniverse uu,Element xml)
{
   super(uu,xml);

   zip_code = IvyXml.getAttrString(xml,"ZIPCODE",DEFAULT_ZIP);

   initialize();
}



private void initialize()
{
   setAccess(weather_url,cond_select);

   BasisParameter pp = BasisParameter.createStringParameter("WeatherCondition");
   pp.setIsSensor(true);
   pp.setIsContinuous(true);
   addParameter(pp);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   return for_universe.getName() + NSEP + "WeatherCondition";
}


@Override public String getLabel()
{
   return "Weather condition";
}

@Override public String getDescription()
{
   return "Weather Condition at zip code " + zip_code;
}



@Override protected String expandUrl(String orig)
{
   Map<String,String> zmap = new HashMap<String,String>();
   zmap.put("ZIP",zip_code);
   zmap.put("APPID","4ed79476e5b66b99b80df09fc1c4dad0");
   
   String url = IvyFile.expandText(orig,zmap);

   return url;
}



@Override protected String decodeWebResponse(String cnts)
{
   try {
      JSONObject obj = new JSONObject(cnts);
      JSONObject wea = obj.getJSONArray("weather").getJSONObject(0);
      String desc = wea.getString("main");
      return desc;
    }
   catch (Throwable t) {
      return null;
    }
}



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override protected void outputLocalXml(IvyXmlWriter xw)
{
   xw.field("ZIPCODE",zip_code);
   super.outputLocalXml(xw);
}



}	// end of class SmartSignWeatherCondSensor




/* end of SmartSignWeatherCondSensor.java */

