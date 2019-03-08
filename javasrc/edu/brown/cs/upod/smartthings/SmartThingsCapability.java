/********************************************************************************/
/*										*/
/*		SmartThingsCapability.java					*/
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



package edu.brown.cs.upod.smartthings;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.json.JSONObject;

import edu.brown.cs.upod.basis.*;
import edu.brown.cs.upod.upod.*;

abstract class SmartThingsCapability extends BasisCapability
{



/********************************************************************************/
/*										*/
/*	Static methods								*/
/*										*/
/********************************************************************************/

static void addCapabilities(BasisUniverse uu)
{
   uu.addCapability(new AccelerationSensor());
   uu.addCapability(new Actuator());
   uu.addCapability(new Alarm());
   uu.addCapability(new Battery());
   uu.addCapability(new Beacon());
   uu.addCapability(new Button());
   uu.addCapability(new CO2Detector());
   uu.addCapability(new ColorControl());
   uu.addCapability(new Configuration());
   uu.addCapability(new ContactSensor());
   uu.addCapability(new DoorControl());
   uu.addCapability(new EnergyMeter());
   uu.addCapability(new IlluminanceMeasurement());
   uu.addCapability(new ImageCapture());
   uu.addCapability(new Lock());
   uu.addCapability(new Momentary());
   uu.addCapability(new MotionSensor());
   uu.addCapability(new MusicPlayer());
   uu.addCapability(new Notification());
   uu.addCapability(new Polling());
   uu.addCapability(new PowerMeter());
   uu.addCapability(new PresenceSensor());
   uu.addCapability(new Refresh());
   uu.addCapability(new RelativeHumidity());
   uu.addCapability(new RelaySwitch());
   uu.addCapability(new Sensor());
   uu.addCapability(new SignalStrength());
   uu.addCapability(new SleepSensor());
   uu.addCapability(new SmokeDetector());
   uu.addCapability(new SpeechSynthesis());
   uu.addCapability(new StepSensor());
   uu.addCapability(new Switch());
   uu.addCapability(new SwitchLevel());
   uu.addCapability(new Temperature());
   uu.addCapability(new Thermostat());
   uu.addCapability(new ThermostatCoolingSetpoint());
   uu.addCapability(new ThermostatFanMode());
   uu.addCapability(new ThermostatHeatingSetpoint());
   uu.addCapability(new ThermostatMode());
   uu.addCapability(new ThermostatOperatingState());
   uu.addCapability(new ThermostatSetpoint());
   uu.addCapability(new ThreeAxis());
   uu.addCapability(new Tone());
   uu.addCapability(new TouchSensor());
   uu.addCapability(new Valve());
   uu.addCapability(new WaterSensor());
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	access_name;

private static Object command_lock = new Object();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SmartThingsCapability(String name,String access)
{
   super(name);

   access_name = access;
}




/********************************************************************************/
/*										*/
/*	Local access methods							*/
/*										*/
/********************************************************************************/

String getAccessName()			{ return access_name; }



/********************************************************************************/
/*										*/
/*	Default setting methods 						*/
/*										*/
/********************************************************************************/

void handleSmartThingsValue(SmartThingsDevice std,Object v)
{
   UpodParameter up = std.findParameter(getParameterName());
   if (up != null) {
      std.setValueInWorld(up,v,null);
    }
   else {
      BasisLogger.logD("SET VALUE FROM SMARTTHINGS: " + getAccessName() + " " +
	    v.getClass().getName() + " " + std.getName() + " " + v);
    }
}



String getParameterName()		{ return getAccessName(); }



/********************************************************************************/
/*										*/
/*	Acceleration Sensor Capability						*/
/*										*/
/********************************************************************************/

private static class AccelerationSensor extends SmartThingsCapability {

   private static enum AccelerationState { ACTIVE, INACTIVE };

   AccelerationSensor() {
      super("Acceleration Sensor","acceleration");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("acceleration",AccelerationState.INACTIVE);
      bp.setLabel(d.getLabel() + " Acceleration");
      addSensor(d,bp);
    }

}	// end of inner class AccelerationSensor



/********************************************************************************/
/*										*/
/*	Actuator capability							*/
/*										*/
/********************************************************************************/

private static class Actuator extends SmartThingsCapability {

   Actuator() {
      super("Actuator","actuator");
    }

   @Override public void addToDevice(UpodDevice e) { }

}	// end of inner class Actuator






/********************************************************************************/
/*										*/
/*	Alarm Capability							*/
/*										*/
/********************************************************************************/

private static class Alarm extends SmartThingsCapability {

   private static enum AlarmState { OFF, SIREN, STROBE, BOTH };

   Alarm() {
      super("Alarm","alarm");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("alarm",AlarmState.OFF);
      bp.setDescription(d.getLabel() + " State");
      bp.setLabel(d.getLabel());
      addSensor(d,bp);
      addSmartThingsTransition(d,bp,AlarmState.STROBE,"Set Strobe","strobe");
      addSmartThingsTransition(d,bp,AlarmState.SIREN,"Set Siren","siren");
      addSmartThingsTransition(d,bp,AlarmState.OFF,"Set Off","off");
      addSmartThingsTransition(d,bp,AlarmState.BOTH,"Set Strobe and Siren","both");
    }

}	// end of inner class Alarm



/********************************************************************************/
/*										*/
/*	Battery Capability							*/
/*										*/
/********************************************************************************/

private static class Battery extends SmartThingsCapability {

  Battery() {
     super("Battery","battery");
   }

  @Override public void addToDevice(UpodDevice d) {
     BasisParameter bp = BasisParameter.createIntParameter("battery",0,100);
     bp.setLabel(d.getLabel() + " Battery");
     addSensor(d,bp);
   }

}	// end of inner class Battery




/********************************************************************************/
/*										*/
/*	Beacon capability							*/
/*										*/
/********************************************************************************/

private static class Beacon extends SmartThingsCapability {

   enum BEACON_STATE { PRESENT, NOT_PRESENT };

   Beacon() {
      super("Beacon","beacon");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("presence",BEACON_STATE.PRESENT);
      bp.setLabel(d.getLabel() + " Presence");
      addSensor(d,bp);
    }

   @Override String getParameterName()		{ return "presence"; }

}	// end of inner class Beacon




/********************************************************************************/
/*										*/
/*	Button Capability							*/
/*										*/
/********************************************************************************/

private static class Button extends SmartThingsCapability {

   private static enum ButtonState { NONE, HELD, PUSHED };

   Button() {
      super("Button","button");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("button",ButtonState.NONE);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
    }

}	// end of inner class Button



/********************************************************************************/
/*										*/
/*	CO2 Detector Capability 						*/
/*										*/
/********************************************************************************/

private static class CO2Detector extends SmartThingsCapability {

   private static enum C02State { TESTED, CLEAR, DETECTED };

   CO2Detector() {
      super("Carbon Monoxide Detector","carbonMonoxideDetector");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("carbonMonoxide",
	    C02State.CLEAR);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
    }

   @Override String getParameterName()		{ return "carbonMonoxide"; }

}	// end of inner class C02Detector



/********************************************************************************/
/*										*/
/*	Color Control capability						*/
/*										*/
/********************************************************************************/

private static class ColorControl extends SmartThingsCapability {

   ColorControl() {
      super("Color Control","colorControl");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bphue = BasisParameter.createRealParameter("hue",0,100);
      bphue.setLabel(d.getLabel() + " Hue");
      BasisParameter bpsat = BasisParameter.createRealParameter("saturation",0,100);
      bpsat.setLabel(d.getLabel() + " Saturation");
      BasisParameter bpcol = BasisParameter.createColorParameter("color");
      bpcol.setLabel(d.getLabel() + " Color");
      addTarget(d,bphue);
      addTarget(d,bpsat);
      addTarget(d,bpcol);
      BasisParameter sethue = BasisParameter.createRealParameter("hue",0,100);
      addSmartThingsTransition(d,bphue,null,"Set Hue","setHue",sethue,0,true);
      BasisParameter setsat = BasisParameter.createRealParameter("saturation",0,100);
      addSmartThingsTransition(d,bpsat,null,"Set Saturation","setSaturation",setsat,100,true);
      BasisParameter setcol = BasisParameter.createColorParameter("color");
      addSmartThingsTransition(d,bpcol,null,"Set Color","setColor",setcol,Color.WHITE,true);
    }

}	// end of inner class ColorControl




/********************************************************************************/
/*										*/
/*	Configure Capability							*/
/*										*/
/********************************************************************************/

private static class Configuration extends SmartThingsCapability {

   Configuration() {
      super("Configuration","configuration");
    }

   @Override public void addToDevice(UpodDevice d) {
      addSmartThingsTransition(d,null,null,"Configure","configure");
    }

}	// end of inner class Configuration



/********************************************************************************/
/*										*/
/*	Contact Sensor Capability						*/
/*										*/
/********************************************************************************/

private static class ContactSensor extends SmartThingsCapability {

   private static String [] CONTACT_STATE = { "open", "closed" };

   ContactSensor() {
      super("Contact Sensor","contact");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("contact",CONTACT_STATE);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
    }
}





/********************************************************************************/
/*										*/
/*	Door Control Capability 						*/
/*										*/
/********************************************************************************/

private static class DoorControl extends SmartThingsCapability
{

   private enum DoorState { UNKNOWN, CLOSED, OPEN, CLOSING, OPENING };

   DoorControl() {
      super("Door Control","doorControl");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("door",DoorState.UNKNOWN);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
      addSmartThingsTransition(d,bp,DoorState.OPEN,"Open","open");
      addSmartThingsTransition(d,bp,DoorState.CLOSED,"Close","close");
    }

   String getParameterName()			{ return "door"; }

}	// end of inner class DoorControl



/********************************************************************************/
/*										*/
/*	Energy Meter Capability 						*/
/*										*/
/********************************************************************************/

private static class EnergyMeter extends SmartThingsCapability
{
   EnergyMeter() {
      super("Energy Meter","energyMeter");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createRealParameter("energy",0,1000000);
      bp.setLabel(d.getLabel() + " Reading");
      addSensor(d,bp);
    }

   String getParameterName()			{ return "energy"; }

}	// end of inner class EnergyMeter




/********************************************************************************/
/*										*/
/*	Illuminance Measurement Capability					*/
/*										*/
/********************************************************************************/

private static class IlluminanceMeasurement extends SmartThingsCapability {

   IlluminanceMeasurement() {
      super("Illuminance Measurement","illuminanceMeasurement");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createRealParameter("illuminance",0,100);
      bp.setLabel(d.getLabel() + " Illuminance");
      addSensor(d,bp);
    }

   @Override String getParameterName()		{ return "illuminance"; }

}


/********************************************************************************/
/*										*/
/*	Image Capture Capability						*/
/*										*/
/********************************************************************************/

private static class ImageCapture extends SmartThingsCapability {

   ImageCapture() {
      super("Image Capture",null);
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createStringParameter("image");
      bp.setLabel(d.getLabel() + " Image");
      addSensor(d,bp);
      addSmartThingsTransition(d,null,null,"Capture Image","take");
    }

}	// end of inner class ImageCapture



/********************************************************************************/
/*										*/
/*	Lock Capability 							*/
/*										*/
/********************************************************************************/

private static class Lock extends SmartThingsCapability {

   private enum LockState { LOCKED, UNLOCKED };

   Lock() {
      super("Lock","lock");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("lock",LockState.LOCKED);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
      addSmartThingsTransition(d,bp,LockState.LOCKED,"Lock","lock");
      addSmartThingsTransition(d,bp,LockState.UNLOCKED,"Unlock","unlock");
    }

}	// end of inner class Lock



/********************************************************************************/
/*										*/
/*	Momentary Capability							*/
/*										*/
/********************************************************************************/

private static class Momentary extends SmartThingsCapability {

   Momentary() {
      super("Momentary","momentary");
    }

   @Override public void addToDevice(UpodDevice d) {
      addSmartThingsTransition(d,null,null,"Push","push");
    }

}	// end of inner class Momentary




/********************************************************************************/
/*										*/
/*	Motion Sensor Capability						*/
/*										*/
/********************************************************************************/

private static class MotionSensor extends SmartThingsCapability {

   private static enum MotionState { ACTIVE, INACTIVE };

   MotionSensor() {
      super("Motion Sensor","motion");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("motion",MotionState.INACTIVE);
      bp.setLabel(d.getLabel() + " Motion");
      addSensor(d,bp);
    }

}	// end of inner class MotionSensor



/********************************************************************************/
/*										*/
/*	Music Player Capability 						*/
/*										*/
/********************************************************************************/

private static class MusicPlayer extends SmartThingsCapability {

   private static enum MuteState { MUTED, UNMUTED };

   MusicPlayer() {
      super("Music Player",null);
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp1 = BasisParameter.createStringParameter("status");
      bp1.setLabel(d.getLabel() + " Status");
      addSensor(d,bp1);
      BasisParameter bp2 = BasisParameter.createIntParameter("level",0,100);
      bp2.setLabel(d.getLabel() + " Level");
      addSensor(d,bp2);
      BasisParameter bp3 = BasisParameter.createStringParameter("trackDescription");
      bp3.setLabel(d.getLabel() + " Track Description");
      addSensor(d,bp3);
      BasisParameter bp4 = BasisParameter.createJSONParameter("trackData");
      bp4.setLabel(d.getLabel() + " Track Data");
      addSensor(d,bp4);
      BasisParameter bp5 = BasisParameter.createEnumParameter("mute",MuteState.UNMUTED);
      addSensor(d,bp5);
      addSmartThingsTransition(d,null,null,"Play","play");
      addSmartThingsTransition(d,null,null,"Pause","pause");
      addSmartThingsTransition(d,null,null,"Stop","stop");
      addSmartThingsTransition(d,null,null,"Next Track","nextTrack");
      BasisParameter setlvl = BasisParameter.createIntParameter("level",0,100);
      addSmartThingsTransition(d,bp2,null,"Set Level","setLevel",setlvl,50);
      BasisParameter settxt = BasisParameter.createStringParameter("text");
      addSmartThingsTransition(d,null,null,"Speak","playText",settxt,null);
      addSmartThingsTransition(d,bp5,MuteState.MUTED,"Mute","mute");
      addSmartThingsTransition(d,null,null,"Previous Track","previousTrack");
      addSmartThingsTransition(d,bp5,MuteState.UNMUTED,"Unmute","unmute");
      // addCallTransition(d,"playTrack",new Class [] { String.class });
      // addCallTransition(d,"setTrack",new Class [] { String.class });
      // addCallTransition(d,"resumeTrack",new Class [] { Map.class });
      // addCallTransition(d,"restoreTrack",new Class [] { Map.class });
    }

}	// end of inner class MusicPlayer





/********************************************************************************/
/*										*/
/*	Notification Capability 						*/
/*										*/
/********************************************************************************/

private static class Notification extends SmartThingsCapability {

   Notification() {
      super("Notification","notification");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter nottxt = BasisParameter.createStringParameter("text");
      nottxt.setLabel(d.getLabel() + " Text");
      addSmartThingsTransition(d,null,null,"Send Notification","deviceNotification",nottxt,null);
    }

}	// end of inner class Notification




/********************************************************************************/
/*										*/
/*	Polling Capbaility							*/
/*										*/
/********************************************************************************/

private static class Polling extends SmartThingsCapability {

   Polling() {
      super("Polling","polling");
    }

   @Override public void addToDevice(UpodDevice d) {
      addSmartThingsTransition(d,null,null,"Poll","poll");
    }

}	// end of inner class Polling



/********************************************************************************/
/*										*/
/*	Power Meter Capability							*/
/*										*/
/********************************************************************************/

private static class PowerMeter extends SmartThingsCapability {

   PowerMeter() {
      super("Power Meter","powerMeter");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("power",0,10000000);
      bp.setLabel(d.getLabel() + " Reading");
      addSensor(d,bp);
    }

   @Override String getParameterName()		{ return "power"; }

}	// end of inner class PowerMeter




/********************************************************************************/
/*										*/
/*	Presense Sensor Capabilty						*/
/*										*/
/********************************************************************************/

private static class PresenceSensor extends SmartThingsCapability {

   static private String [] PresenceState = { "present", "not present" };

   PresenceSensor() {
      super("Presence Sensor","presence");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("presence",PresenceState);
      bp.setLabel(d.getLabel() + " Presence");
      addSensor(d,bp);
    }

}	// end of inner class PresenceSensor




/********************************************************************************/
/*										*/
/*	Refresh Capabilty							*/
/*										*/
/********************************************************************************/

private static class Refresh extends SmartThingsCapability {

   Refresh() {
      super("Refresh","refresh");
    }

   @Override public void addToDevice(UpodDevice d) {
      addSmartThingsTransition(d,null,null,"Refresh","refresh");
    }

}	// end of inner class Refresh




/********************************************************************************/
/*										*/
/*	Relative Humidity Measurement Capabilty 				*/
/*										*/
/********************************************************************************/

private static class RelativeHumidity extends SmartThingsCapability {

   RelativeHumidity() {
      super("Relative Humidity Measurement","relativeHumidityMeasurement");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("humidity",0,100);
      bp.setLabel(d.getLabel() + " Humidity");
      addSensor(d,bp);
    }

   @Override String getParameterName()		{ return "humidity"; }

}	// end of inner class RelativeHumidity




/********************************************************************************/
/*										*/
/*	Relay Switch Capabilty							*/
/*										*/
/********************************************************************************/

private static class RelaySwitch extends SmartThingsCapability {

   private static enum SWITCH_STATE { OFF, ON };

   RelaySwitch() {
      super("Relay Switch","relaySwitch");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("switch",SWITCH_STATE.OFF);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
      addSmartThingsTransition(d,bp,SWITCH_STATE.OFF,"Turn Off","off");
      addSmartThingsTransition(d,bp,SWITCH_STATE.ON,"Turn On","on");
    }

}	// end of inner class RelaySwitch




/********************************************************************************/
/*										*/
/*	Relay Switch Capabilty							*/
/*										*/
/********************************************************************************/

private static class Sensor extends SmartThingsCapability {

   Sensor() {
      super("Sensor","sensor");
    }

   @Override public void addToDevice(UpodDevice d) { }

}	// end of inner class Sensor




/********************************************************************************/
/*										*/
/*	Signal Strength Capabilty						*/
/*										*/
/********************************************************************************/

private static class SignalStrength extends SmartThingsCapability {

   SignalStrength() {
      super("Signal Strength","signalStrength");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp1 = BasisParameter.createRealParameter("lqi",0,1000000);
      bp1.setLabel(d.getLabel() + " Link Quality");
      addSensor(d,bp1);
      BasisParameter bp2 = BasisParameter.createRealParameter("rssi",0,1000000);
      bp2.setLabel(d.getLabel() + " Signal Strength");
      addSensor(d,bp2);
    }

}	// end of inner class SignalStrength




/********************************************************************************/
/*										*/
/*	Sleep Sensor								*/
/*										*/
/********************************************************************************/


private static class SleepSensor extends SmartThingsCapability {

   static private String [] SleepState = { "not sleeping", "sleeping" };

   SleepSensor() {
      super("Sleep Sensor","sleepSensor");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("sleepSensor",SleepState);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
    }

}	// end of inner class SleepSensor




/********************************************************************************/
/*										*/
/*	Smoke Detector Capability						*/
/*										*/
/********************************************************************************/


private static class SmokeDetector extends SmartThingsCapability {

   static private String [] SMOKE_STATE = { "detected", "clear", "tested" };

   SmokeDetector() {
      super("Smoke Detector","smokeDetector");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("smokeDetector",SMOKE_STATE);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
    }

}	// end of inner class SmokeDetector




/********************************************************************************/
/*										*/
/*	Speech Synthesis Capability						*/
/*										*/
/********************************************************************************/

private static class SpeechSynthesis extends SmartThingsCapability {

   SpeechSynthesis() {
      super("Speech Synthesis",null);
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter spk = BasisParameter.createStringParameter("text");
      spk.setLabel(d.getLabel() + " Text");
      addSmartThingsTransition(d,null,null,"Speak","speak",spk,null);
    }

}	// end of inner class SpeechSynthesis




/********************************************************************************/
/*										*/
/*	Step Sensor Capabilty							*/
/*										*/
/********************************************************************************/

private static class StepSensor extends SmartThingsCapability {

   StepSensor() {
      super("Step Sensor","stepSensor");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp1 = BasisParameter.createIntParameter("steps",0,1000000);
      bp1.setLabel(d.getLabel() + " Steps");
      addSensor(d,bp1);
      BasisParameter bp2 = BasisParameter.createRealParameter("goal",0,1000000);
      bp2.setLabel(d.getLabel() + " Goal");
      addSensor(d,bp2);
    }

}	// end of inner class StepSensor




/********************************************************************************/
/*										*/
/*	Switch Capabilty							*/
/*										*/
/********************************************************************************/

private static class Switch extends SmartThingsCapability {

   private enum SWITCH_STATE { OFF, ON };

   Switch() {
      super("Switch","switch");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("switch",SWITCH_STATE.OFF);
      bp.setDescription(d.getLabel() + " state");
      bp.setLabel(d.getLabel() + " switch");
      bp.setIsTarget(true);
      addSensor(d,bp);
      addSmartThingsTransition(d,bp,SWITCH_STATE.ON,"Turn On","on");
      addSmartThingsTransition(d,bp,SWITCH_STATE.OFF,"Turn Off","off");
    }

}	// end of inner class Switch




/********************************************************************************/
/*										*/
/*	Switch Level Capabilty							*/
/*										*/
/********************************************************************************/

private static class SwitchLevel extends SmartThingsCapability {

   SwitchLevel() {
      super("Switch Level","switchLevel");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("switch",0,100);
      bp.setLabel(d.getLabel() + " Level");
      bp.setIsTarget(true);
      addSensor(d,bp);
      BasisParameter lvlset = BasisParameter.createIntParameter("level",0,100);
      addSmartThingsTransition(d,bp,null,"Set Level","setLevel",lvlset,50);
    }

}	// end of inner class SwitchLevel




/********************************************************************************/
/*										*/
/*	Temperature Measurement Capabilty					*/
/*										*/
/********************************************************************************/

private static class Temperature extends SmartThingsCapability {

   Temperature() {
      super("Temperature Measurement","temperature");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("temperature",0,10000);
      bp.setLabel(d.getLabel() + " Temperature");
      addSensor(d,bp);
    }

}	// end of inner class Temperature




/********************************************************************************/
/*										*/
/*	Thermostat Capability							*/
/*										*/
/********************************************************************************/


private static enum THERMOSTAT_MODE { AUTO, EMERGENCY_HEAT, HEAT, OFF, COOL };
private static enum FAN_MODE { AUTO, ON, CIRCULATE };
private static String [] THERMOSTAT_STATE = { "heating", "idle", "pending cool",
   "vent economizer", "cooling", "pending heat", "fan only" };



private static class Thermostat extends SmartThingsCapability {

   Thermostat() {
      super("Thermostat","thermostat");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp1 = BasisParameter.createIntParameter("temperatore",0,100);
      bp1.setLabel(d.getLabel() + " Temperature");
      addSensor(d,bp1);
      BasisParameter bp2 = BasisParameter.createIntParameter("heatingSetPoint",0,100);
      bp2.setLabel(d.getLabel() + " Heating Set Point");
      addTarget(d,bp2);
      BasisParameter bp3 = BasisParameter.createIntParameter("coolingSetPoint",0,100);
      bp3.setLabel(d.getLabel() + " Cooling Set Point");
      addTarget(d,bp3);
      BasisParameter bp4 = BasisParameter.createIntParameter("thermostatSetPoint",0,100);
      bp4.setLabel(d.getLabel() + " Set Point");
      addTarget(d,bp4);
      BasisParameter bp5 = BasisParameter.createEnumParameter("thermostatMode",THERMOSTAT_MODE.AUTO);
      bp5.setLabel(d.getLabel() + " Mode");
      addSensor(d,bp5);
      BasisParameter bp6 = BasisParameter.createEnumParameter("thermostatFanMode",FAN_MODE.AUTO);
      bp6.setLabel(d.getLabel() + " Fan Mode");
      addSensor(d,bp6);
      BasisParameter bp7 = BasisParameter.createEnumParameter("thermostatOperatingState",THERMOSTAT_STATE);
      bp7.setLabel(d.getLabel() + " Operating State");
      addSensor(d,bp7);
      BasisParameter setpt= BasisParameter.createIntParameter("Set Point",40,100);
      addSmartThingsTransition(d,bp2,null,"Set Heating Set Point","setHeatingSetpoint",setpt,68);
      addSmartThingsTransition(d,bp2,null,"Set Cooling Set Point","setCoolingSetpoint",setpt,68);
      BasisParameter modset = BasisParameter.createEnumParameter("Mode",THERMOSTAT_MODE.AUTO);
      addSmartThingsTransition(d,bp5,null,"Set Mode","setThermostateMode",modset,THERMOSTAT_MODE.AUTO);
      BasisParameter fanset = BasisParameter.createEnumParameter("Fan Mode",FAN_MODE.AUTO);
      addSmartThingsTransition(d,bp6,null,"Set Fan Mode","setThermostatFanMode",fanset,FAN_MODE.AUTO);
    }

}	// end of inner class Thermostat




/********************************************************************************/
/*										*/
/*	Thermostat Cooling Setpoint Capabilty					*/
/*										*/
/********************************************************************************/

private static class ThermostatCoolingSetpoint extends SmartThingsCapability {

   ThermostatCoolingSetpoint() {
      super("Thermostat Cooling Setpoint","thermostatCoolingSetpoint");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("coolingSetpoint",0,100);
      bp.setLabel(d.getLabel() + " Cooling Set Point");
      addSensor(d,bp);
      BasisParameter setpt = BasisParameter.createIntParameter("setpt",40,100);
      addSmartThingsTransition(d,bp,null,"Set Cooling Set Point","setCoolingSetpoint",setpt,78);
    }

}	// end of inner class ThermostatCoolingSetpoint




/********************************************************************************/
/*										*/
/*	Thermostat Fan Mode Capabilty						*/
/*										*/
/********************************************************************************/

private static class ThermostatFanMode extends SmartThingsCapability {

   ThermostatFanMode() {
      super("Thermostat Fan Mode","thermostatFanMode");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("thermostatFanMode",FAN_MODE.ON);
      addSensor(d,bp);
      BasisParameter fanmode = BasisParameter.createEnumParameter("Fan Mode",FAN_MODE.AUTO);
      addSmartThingsTransition(d,bp,null,"Fan Mode","setThermostatFanMode",fanmode,FAN_MODE.AUTO);
    }

}	// end of inner class ThermostatFanMode



/********************************************************************************/
/*										*/
/*	Thermostat Heating Setpoint Capabilty					*/
/*										*/
/********************************************************************************/

private static class ThermostatHeatingSetpoint extends SmartThingsCapability {

   ThermostatHeatingSetpoint() {
      super("Thermostat Heating Setpoint","thermostatHeadingSetpoint");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("heatingSetpoint",0,100);
      bp.setLabel(d.getLabel() + " Heating Set Point");
      addSensor(d,bp);
      BasisParameter setpt = BasisParameter.createIntParameter("setpt",40,100);
      addSmartThingsTransition(d,bp,null,"Set Heating Set Point","setHeatingSetpoint",setpt,68);
    }

}	// end of inner class ThermostatHeatingSetpoint




/********************************************************************************/
/*										*/
/*	Thermostat Mode Capabilty						*/
/*										*/
/********************************************************************************/

private static class ThermostatMode extends SmartThingsCapability {

   ThermostatMode() {
      super("Thermostat Mode","thermostatMode");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("thermostatMode",THERMOSTAT_MODE.OFF);
      bp.setLabel(d.getLabel() + " Mode");
      addTarget(d,bp);
      BasisParameter tmp = BasisParameter.createEnumParameter("Mode",THERMOSTAT_MODE.AUTO);
      addSmartThingsTransition(d,bp,null,"Set Mode","setThermostatMode",tmp,THERMOSTAT_MODE.AUTO);
    }

}	// end of inner class ThermostatFanMode




/********************************************************************************/
/*										*/
/*	Thermostat Operating State Capability					*/
/*										*/
/********************************************************************************/

private static class ThermostatOperatingState extends SmartThingsCapability {

   ThermostatOperatingState() {
      super("Thermostat Operating State","thermostatOperatingState");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp7 = BasisParameter.createEnumParameter("thermostatOperatingState",THERMOSTAT_STATE);
      bp7.setLabel(d.getLabel() + " Operating State");
      addSensor(d,bp7);
    }

}	// end of inner class ThermostatOperatingState




/********************************************************************************/
/*										*/
/*	Thermostat Setpoint Capabilty						*/
/*										*/
/********************************************************************************/

private static class ThermostatSetpoint extends SmartThingsCapability {

   ThermostatSetpoint() {
      super("Thermostat Setpoint","thermostatSetpoint");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("thermostatSetpoint",0,100);
      bp.setLabel(d.getLabel() + " Set Point");
      addSensor(d,bp);
    }

}	// end of inner class ThermostatSetpoint


/********************************************************************************/
/*										*/
/*	ThreeAxis Sensor							*/
/*										*/
/********************************************************************************/

private static class ThreeAxis extends SmartThingsCapability {

   ThreeAxis() {
      super("ThreeAxis","threeAxis");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bpx = BasisParameter.createRealParameter("x");
      bpx.setLabel(d.getLabel() + " X");
      BasisParameter bpy = BasisParameter.createRealParameter("y");
      bpy.setLabel(d.getLabel() + " Y");
      BasisParameter bpz = BasisParameter.createRealParameter("z");
      bpz.setLabel(d.getLabel() + " Z");
      addSensor(d,bpx);
      addSensor(d,bpy);
      addSensor(d,bpz);
    }

}	// end of inner class ThreeAxis



/********************************************************************************/
/*										*/
/*	Tone Capability 							*/
/*										*/
/********************************************************************************/

private static class Tone extends SmartThingsCapability {

   Tone() {
      super("Tone","tone");
    }

   @Override public void addToDevice(UpodDevice d) {
      addSmartThingsTransition(d,null,null,"Beep","beep");
    }

}	// end of inner class Tone



/********************************************************************************/
/*										*/
/*	Touch Sensor Capability 						*/
/*										*/
/********************************************************************************/

private static class TouchSensor extends SmartThingsCapability {

   private static enum TouchState { TOUCHED, NOT_TOUCHED };

   TouchSensor() {
      super("Touch Sensor","touch");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("touch",TouchState.NOT_TOUCHED);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
    }

}	// end of inner class TouchSensor



/********************************************************************************/
/*										*/
/*	Valve Capability							*/
/*										*/
/********************************************************************************/

private static class Valve extends SmartThingsCapability {

   private static enum VALVE_STATE { CLOSED, OPEN };

   Valve() {
      super("Valve","valve");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("state",VALVE_STATE.CLOSED);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
      addSmartThingsTransition(d,bp,VALVE_STATE.OPEN,"Open","open");
      addSmartThingsTransition(d,bp,VALVE_STATE.CLOSED,"Close","close");
    }

}	// end of inner class Valve




/********************************************************************************/
/*										*/
/*	Water Sensor Capability 						*/
/*										*/
/********************************************************************************/


private static class WaterSensor extends SmartThingsCapability {

   private static enum WaterState { DRY, WET };

   WaterSensor() {
      super("Water Sensor","waterSensor");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("waterSensor",WaterState.DRY);
      bp.setLabel(d.getLabel() + " State");
      addSensor(d,bp);
    }

}	// end of inner class WaterSensor




/********************************************************************************/
/*										*/
/*	Field set transition							*/
/*										*/
/********************************************************************************/

protected void addSmartThingsTransition(UpodDevice ud,UpodParameter p,Object state,
      String lbl,String rtn)
{
   addSmartThingsTransition(ud,p,state,lbl,rtn,null,null,false);
}


protected void addSmartThingsTransition(UpodDevice ud,UpodParameter p,Object state,
      String lbl,String rtn,boolean force)
{
   addSmartThingsTransition(ud,p,state,lbl,rtn,null,null,force);
}


protected void addSmartThingsTransition(UpodDevice ud,UpodParameter p,Object state,
      String lbl,String rtn,BasisParameter tp,Object dflt)
{
   addSmartThingsTransition(ud,p,state,lbl,rtn,tp,dflt,false);
}


protected void addSmartThingsTransition(UpodDevice ud,UpodParameter p,Object state,
      String lbl,String rtn,BasisParameter tp,Object dflt,boolean force)
{
   SmartThingsDevice std = (SmartThingsDevice) ud;
   SetTransition st = new SetTransition(lbl,p,state,rtn,tp,dflt,force);
   std.addTransition(st);
}



private class SetTransition extends BasisTransition {

   private String transition_name;
   private String transition_label;
   private Object field_value;
   private UpodParameter for_parameter;
   private String routine_name;
   private boolean force_on;

   SetTransition(String name,UpodParameter p,Object value,String rtn,UpodParameter tp,
		    Object tval,boolean force) {
      transition_label = name;
      transition_name = name.replaceAll(" ",NSEP);
      for_parameter = p;
      field_value = value;
      routine_name = rtn;
      if (tp != null) {
	 addParameter(tp,tval);
       }
      force_on = force;
    }

   @Override public String getName()		{ return transition_name; }
   @Override public String getLabel()		{ return transition_label; }
   @Override public String getDescription()	{ return transition_label; }
   @Override public Type getTransitionType()	{ return Type.STATE_CHANGE; }

   @Override public void perform(UpodWorld w,UpodDevice d,UpodPropertySet params)
   throws UpodActionException {
      if (d == null) throw new UpodActionException("No entity to act on");
      if (w == null) throw new UpodActionException("No world to act in");
      SmartThingsDevice std = (SmartThingsDevice) d;
      SmartThingsUniverse stu = (SmartThingsUniverse) d.getUniverse();

      synchronized (command_lock) {
	 BasisLogger.logD("START PERFORM " + transition_name + " " + routine_name + " " +
			     field_value + " " + for_parameter + " " + force_on);

	 if (force_on) forceOn(w,std);

	 if (w.isCurrent()) {
	    JSONObject rqst = new JSONObject();
	    rqst.put("call",routine_name);
	    if (params != null) {
	       for (Map.Entry<String,Object> ent : params.entrySet()) {
		  Object o = ent.getValue();
		  String nm = ent.getKey();
		  BasisLogger.logD("CALL PARAMETER: " + nm + " " + o.getClass() + " " + o);
		  if (o instanceof Color) {
		     Color c = (Color) o;
		     StringWriter sw = new StringWriter();
		     PrintWriter pw = new PrintWriter(sw);
		     pw.format("#%02x%02x%02x",c.getRed(),c.getGreen(),c.getBlue());
		     float [] hsb = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),null);
		     JSONObject obj = new JSONObject();
		     obj.put("hex",sw.toString());
		     obj.put("hue",hsb[0]);
		     obj.put("saturation",hsb[1]);
		     obj.put("level",hsb[2]);
		     rqst.put(nm,obj);
		   }
		  else if (o instanceof Enum) {
		     String obj = o.toString();
		     obj = obj.toLowerCase();
		     rqst.put(nm,o);
		   }
		  else {
		     rqst.put(nm,o);
		   }
		}
	     }
	    stu.sendCommand(getAccessName(),std,rqst);
	  }
	 else if (for_parameter != null) {
	    if (field_value != null) {
	       d.setValueInWorld(for_parameter,field_value,w);
	     }
	    else {
	       for (Object o : params.values()) {
		  d.setValueInWorld(for_parameter,o,w);
		  break;
		}
	     }
	  }

	 BasisLogger.logD("END PERFORM");
       }
    }

   private void forceOn(UpodWorld w,SmartThingsDevice std) {
      String cmd = null;
      UpodParameter param = null;
      Object value = null;
      String acc = null;

      if (std.hasCapability("Switch")) {
	 cmd = "on";
	 param = std.findParameter("switch");
	 value = "ON";
	 acc = "switch";
       }
      else if (std.hasCapability("Relay Switch")) {
	 cmd = "on";
	 param = std.findParameter("switch");
	 value = "ON";
	 acc = "relaySwitch";
       }
      else {
	 BasisLogger.logD("ATTEMPT TO FORCE ON WITHOUT CAPABILITY");
	 BasisLogger.logD("   DEVICE: " + std);
	 for (UpodCapability stc : std.getCapabilities()) {
	    BasisLogger.logD("   CAP: " + stc.getName());
	  }
       }

     if (cmd == null) return;

      if (w.isCurrent()) {
	 SmartThingsUniverse stu = (SmartThingsUniverse) std.getUniverse();
	 JSONObject rqst = new JSONObject();
	 rqst.put("call",cmd);
	 stu.sendCommand(acc,std,rqst);
       }
      else if (param != null && value != null) {
	 std.setValueInWorld(param,value,w);
       }
    }


}	// end of inner class SetTransition







}	// end of class SmartThingsCapability




/* end of SmartThingsCapability.java */

