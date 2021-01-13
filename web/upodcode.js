/********************************************************************************/
/*                                                                              */
/*              upodcode.js                                                     */
/*                                                                              */
/*      General code for handling upod information                              */
/*                                                                              */
/********************************************************************************/



/********************************************************************************/
/*                                                                              */
/*      Global variables                                                        */
/*                                                                              */
/********************************************************************************/

var rule_set;
var device_set;
var sensor_set;
var entity_set;
var condition_set;
var param_map = new Object();
var USID;


/********************************************************************************/
/*                                                                              */
/*      Initializations                                                         */
/*                                                                              */
/********************************************************************************/

$(function() {
     var x = $('.dateregion');
     if (x.length > 0) x.datepicker({ });
     var y = $('.timeregion');
     if (y.length > 0) y.timepicker({ scrollDefaultNow: true });
});



if (typeof String.prototype.startsWith != 'function') {
   String.prototype.startsWith = function(str) {
      return this.slice(0,str.length) == str;
    };
}

if (typeof String.prototype.endsWith != 'function') {
   String.prototype.endsWith = function(str) {
      return this.slice(-str.length) == str;
    };
}





/********************************************************************************/
/*                                                                              */
/*      Setup standard items on page                                            */
/*                                                                              */
/********************************************************************************/

function setupCommon(world)
{
   var attrs = world['@attributes'];
   var lbl = attrs.LABEL;
   var nam = attrs.NAME;

   $('#topheader').text(nam + ": " + lbl);
   $('#ruleheader').text("Define a New " + nam + " Rule");
   $('#conditionheader').text("Condition for " + nam + " Rule");
   $('#actionheader').text("Action for " + nam + " Rule");
   $('#conflictheader').text("Conflicts for " + nam + " Rule");
}




/********************************************************************************/
/*                                                                              */
/*      Basis commands                                                          */
/*                                                                              */
/********************************************************************************/

function loadDescription(post)
{
   loadJson("<LIST />",function(world) {
              var pgm = world['PROGRAM'];
              rule_set = buildRuleSet(pgm);
              device_set = buildDeviceSet(world);
              sensor_set = buildSensorSet(world);
              entity_set = buildEntitySet(world);
              condition_set = buildConditionSet(world);
              if (post != null) post(world);
            });

}



function loadProgram(post)
{
   loadJson("<LIST_PROGRAM />",function(pgm) {
               rule_set = buildRuleSet(pgm);
               if (post != null) post();
             });
}




/********************************************************************************/
/*                                                                              */
/*      Command Utility Functions                                               */
/*                                                                              */
/********************************************************************************/


function loadJson(cmd,resp)
{
   if (resp == null) loadXml(cmd);
   else {
      loadXml(cmd,function (xml) {
                 if (xml == null) resp(null);
                 else resp(xmlToJson(xml.documentElement)); });
    }
}



function loadXml(cmd,resp)
{
   var req = new XMLHttpRequest();

   req.onreadystatechange = function() {
      if (req.readyState == req.DONE) {
         if (resp != null) {
            if (req.status == 200) resp(req.responseXML);
            else resp(null);
          }
       }
    };

   var txt = "C=" + encodeURIComponent(cmd);
   if (USID != null) txt += "&USID=" + USID;

   req.open("POST","/command",true);
   req.setRequestHeader("Content-type","application/x-www-form-urlencoded");
   req.send(txt);
}




// Changes XML to JSON
function xmlToJson(xml)
{
   // Create the return object
   var obj = {};

   if (xml.nodeType == 1) { // element
      // do attributes
      if (xml.attributes.length > 0) {
         obj["@attributes"] = {};
         for (var j = 0; j < xml.attributes.length; j++) {
            var attribute = xml.attributes.item(j);
            obj["@attributes"][attribute.nodeName] = attribute.nodeValue;
          }
       }
    }
   else if (xml.nodeType == 3) { // text
      obj = xml.nodeValue;
    }
   else if (xml.nodeType == 4) { // cdata
      obj = xml.nodeValue;
    }

   // do children
   if (xml.hasChildNodes()) {
      for(var i = 0; i < xml.childNodes.length; i++) {
         var item = xml.childNodes.item(i);
         var nodeName = item.nodeName;
         if (typeof(obj[nodeName]) == "undefined") {
            obj[nodeName] = xmlToJson(item);
          }
         else {
            if (typeof(obj[nodeName].push) == "undefined") {
               var old = obj[nodeName];
               obj[nodeName] = [];
               obj[nodeName].push(old);
             }
            obj[nodeName].push(xmlToJson(item));
          }
       }
    }
   return obj;
}




/********************************************************************************/
/*                                                                              */
/*      Parameters                                                              */
/*                                                                              */
/********************************************************************************/

function ParamObject(x)
{
   this.param_name = getAttr(x,"NAME");
   this.param_type = getAttr(x,"TYPE");
   this.param_label = decode(getAttr(x,"LABEL"));
   this.param_desc = getTextElement(x,"DESC");
   this.param_id = this.param_name.replace(" ","_").replace(".","_");
   this.param_value = getTextElement(x,"CURRENT");
   this.param_sensor = getAttrBool(x,"ISSENSOR");
   this.param_target = getAttrBool(x,"ISTARGET");
   this.param_min = getAttr(x,"MIN");
   this.param_max = getAttr(x,"MAX");

   this.param_states = [];
   var vals = makeArray(x.VALUE);
   for (var i = 0; i < vals.length; ++i) {
      var v = getText(vals[i]);
      if (this.param_type == "BOOLEAN") v = v.toUpperCase();
      this.param_states.push(v);
    }

   param_map[this.param_id] = this.param_name;

   this.getId = function() { return this.param_id; }
   this.getName = function() { return this.param_name; }
   this.getType = function() { return this.param_type; }
   this.getLabel = function() { return this.param_label; }
   this.getDescription = function() { return this.param_desc; }
   this.getValue = function() { return this.param_value; }
   this.isSensor = function() { return this.param_sensor; }
   this.isTarget = function() { return this.param_target; }
   this.getValues = function() { return this.param_states; }
   this.getMinValue = function() { return this.param_min; }
   this.getMaxValue = function() { return this.param_max; }

}       // end of object ParamObject



/********************************************************************************/
/*                                                                              */
/*      Conditions                                                              */
/*                                                                              */
/********************************************************************************/

function CondObject(x)
{
   this.cond_name = getAttr(x,"NAME");
   this.cond_label = decode(getTextElement(x,"LABEL"));
   this.cond_desc = decode(getTextElement(x,"DESC"));
   this.cond_trigger = getAttrBool(x,"TRIGGER");
   this.cond_type = getAttr(x,"TYPE");
   this.cond_source = x;
   var fxml = makeArray(x.FIELD);
   this.cond_fields = [];
   for (var i = 0; i < fxml.length; ++i) {
      this.cond_fields[i] = new FieldObject(fxml[i]);
    }

   this.getName = function() { return this.cond_name; }
   this.getLabel = function() { return this.cond_label; }
   this.getType = function() { return this.cond_type; }
   this.getDescription = function() { return this.cond_desc; }
   this.isTrigger = function () { return this.cond_trigger; }

   this.getTimeDescription = function() { return decode(getAttr(this.cond_source,"LABEL")); }
   this.getFromDate = function() {
      return dateString(getAttr(this.cond_source.CALEVENT,"FROMDATE"));
    }
   this.getToDate = function() {
      return dateString(getAttr(this.cond_source.CALEVENT,"TODATE"));
    }
   this.getFromTime = function() {
      return timeString(getAttr(this.cond_source.CALEVENT,"FROMTIME"));
    }
   this.getToTime = function() {
      return timeString(getAttr(this.cond_source.CALEVENT,"TOTIME"));
    }
   this.getDays = function() { return getAttr(this.cond_source.CALEVENT,"DAYS"); }
   this.getInterval = function() { return getAttr(this.cond_source.CALEVENT,"INTERVAL"); }

   this.getCalFieldCount = function(i) { return this.cond_fields.length; }
   this.getCalField = function(i)      { return this.cond_fields[i]; }

   this.getLowValue = function() {
      return getAttr(this.cond_source,"LOW");
    }
   this.getHighValue = function() {
      return getAttr(this.cond_source,"HIGH");
    }

   this.getMinValue = function() {
      return getAttr(this.cond_source,"MIX");
    }
   this.getMaxValue = function() {
      return getAttr(this.cond_source,"MAX");
    }

   this.getTimeTrigger = function(what) {
      return getAttr(this.cond_source,what.toUpperCase());
    }


}       // end of object CondObject



function buildConditionSet(world)
{
   var nconds = [];
   var bconds = world.BASICCONDS;
   var condset = makeArray(bconds.CONDITION);
   for (var i = 0; i < condset.length; ++i) {
      var x = condset[i];
      var rule = new CondObject(x);
      nconds.push(rule);
    }

   return nconds;
}



/********************************************************************************/
/*                                                                              */
/*      Rule Set                                                                */
/*                                                                              */
/********************************************************************************/

function ActionObject(x)
{
   this.action_name = getAttr(x,"NAME");
   this.action_label = decode(getTextElement(x,"LABEL"));
   this.action_description = decode(getTextElement(x,"DESC"));
   this.action_entity = getAttr(x.DEVICE,"ID");
   this.action_transition = null;
   if (x.TRANSITION != null) {
      this.action_transition = getAttr(x.TRANSITION,"NAME");
      this.action_type = getAttr(x.TRANSITION,"TYPE");
    }
   this.action_parameters = [];
   var pms = makeArray(x.PARAMETERS.PARAMETER);
   for (var i = 0; i < pms.length; ++i) {
      var p = new ParamObject(pms[i]);
      this.action_parameters.push(p);
    }

   this.getName = function() { return this.action_name; }
   this.getLabel = function() { return this.action_label; }
   this.getDescription = function() { return this.action_description; }
   this.getEntityId = function() { return this.action_entity; }
   this.getTransitionName = function() { return this.action_transition; }

   this.getParameters = function() { return this.action_parameters; }

}       // end of object ActionObject



function RuleObject(x)
{
   this.rule_name = getAttr(x,"NAME");
   this.rule_id = getAttr(x,"ID");
   this.rule_description = decode(getTextElement(x,"LABEL"));
   this.rule_priority = getAttr(x,"PRIORITY");
   this.rule_conditions = [];

   var cnd = x.CONDITION;
   // console.log("CONDITION",cnd);
   if (cnd != null) {
      var typ = getAttr(cnd,"TYPE");
      if (typ == 'AND') {
         var cnds = makeArray(cnd.CONDITION);
         for (var i = 0; i < cnds.length; ++i) {
            var csub = new CondObject(cnds[i]);
            this.rule_conditions.push(csub);
          }
       }
      else {
         var cobj = new CondObject(cnd);
         this.rule_conditions.push(cobj);
       }
    }

   var act = x.ACTIONS;
   this.rule_actions = [];
   var acts = makeArray(act.ACTION);
   for (var i = 0; i < acts.length; ++i) {
      var actv = new ActionObject(acts[i]);
      this.rule_actions.push(actv);
    }

   this.getName = function() { return this.rule_name; };
   this.getId = function() { return this.rule_id; };
   this.getDescription = function() { return this.rule_description; };
   this.getPriority = function() { return this.rule_priority; };
   this.setPriority = function(v) { this.rule_priority = v; }
   this.getConditions = function() { return this.rule_conditions; }
   this.getActions = function() { return this.rule_actions; }

}       // end of object RuleObject;



function FieldObject(x)
{
   this.field_name = getAttr(x,"NAME");
   this.field_null = getAttr(x,"NULL");
   this.field_match = getAttr(x,"MATCH");
   this.field_value = getTextElement(x,"MATCHVALUE");

   this.getName = function() { return this.field_name; }
   this.getNullType = function() { return this.field_null; }
   this.getMatchType = function() { return this.field_match; }
   this.getMatchValue = function() { return this.field_value; }

}       // end of object FieldObject




function buildRuleSet(js)
{
   var nrules = [];
   var ruleset = makeArray(js.RULE);
   for (var i = 0; i < ruleset.length; ++i) {
      var x = ruleset[i];
      var rule = new RuleObject(x);
      nrules.push(rule);
    }
   nrules.sort(function (r1,r2) { return r2.getPriority() - r1.getPriority(); });

   // console.log("RULES",nrules.length,nrules);

   return nrules;
}


function removeRule(js)
{
   var rulelt = js;
   var rid = getAttr(rulelt,"ID");

   for (var i = 0; i < rule_set.length; ++i) {
      var rule = rule_set[i];
      if (rule.getId() == rid) {
         rule_set.splice(i,1);
         break;
       }
    }
}



function findRule(id)
{
   for (var i = 0; i < rule_set.length; ++i) {
      var rule = rule_set[i];
      if (rule.getId() == id) return rule;
      if (rule.getName() == id) return rule;
    }
   return null;
}




/********************************************************************************/
/*                                                                              */
/*      Transition objects                                                      */
/*                                                                              */
/********************************************************************************/

function TransitionObject(ent,x)
{
   this.trans_name = getAttr(x,"NAME");
   this.trans_label = decode(getAttr(x,"LABEL"));
   this.trans_desc = getTextElement(x,"DESC");
   this.trans_entity = ent;
   this.trans_entity_param = getAttr(x,"ENTITYPARAM");

   this.trans_parameters = [];
   var prms = makeArray(x["PARAMETER"]);
   for (var i = 0; i < prms.length; ++i) {
      this.trans_parameters.push(new ParamObject(prms[i]));
    }

   this.getName = function()                    { return this.trans_name; }
   this.getLabel = function()                   { return this.trans_label; }
   this.getDescription = function()             { return this.trans_description; }
   this.getParameters = function()              { return this.trans_parameters; }

}       // end of object TransitionObject




/********************************************************************************/
/*                                                                              */
/*      Device methods                                                          */
/*                                                                              */
/********************************************************************************/

function DeviceObject(x)
{
   this.device_id = getAttr(x,"ID");
   this.device_name = getAttr(x,"NAME");
   this.device_label = decode(getAttr(x,"LABEL"));
   this.device_desc = getTextElement(x,"DESC");
   this.sensor_states = [];
   var sts = makeArray(x["STATE"]);
   for (var i = 0; i < sts.length; ++i) {
      var stx = sts[i];
      this.sensor_states.push(getAttr(stx,"NAME"));
    }

   this.device_parameters = [];
   var prms = makeArray(x["PARAMETER"]);
   for (var i = 0; i < prms.length; ++i) {
      this.device_parameters.push(new ParamObject(prms[i]));
    }
   this.device_transitions = [];
   var trns = makeArray(x["TRANSITION"]);
   for (var i = 0; i < trns.length; ++i) {
      this.device_transitions.push(new TransitionObject(this,trns[i]));
    }

   this.getId = function() { return this.device_id; }
   this.getName = function() { return this.device_name; }
   this.getLabel = function() { return this.device_label; }
   this.getDescription = function() { return this.device_desc; }

   this.getStates = function() { return this.sensor_states; }

   this.getParameters = function()      { return this.device_parameters; }

   this.getParameter = function(nm) {
      for (var i = 0; i < this.device_parameters.length; ++i) {
         var prm = this.device_parameters[i];
         if (prm.getId() == nm || prm.getName() == nm) return prm;
       }
      return null;
    }

   this.getTransitions = function()     { return this.device_transitions; }

   this.getSensors = function() {
      var s = [];
      for (var i = 0; i < this.device_parameters.length; ++i) {
         var prm = this.device_parameters[i];
         if (prm.isSensor()) {
            var sensor = new SensorObject(this,prm);
            s.push(sensor);
          }
       }
      return s;
    }

   this.isTarget = function() {
      if (this.device_transitions.length > 0) return true;
      for (var i = 0; i < this.device_parameters.length; ++i) {
         var prm = this.device_parameters[i];
         if (prm.isTarget()) return true;
       }
      return false;
    }

}       // end of class DeviceObject



function buildDeviceSet(world)
{
   var sens = [];
   var sjs = makeArray(world.DEVICES.DEVICE);
   for (var i = 0; i < sjs.length; ++i) {
      var sen = sjs[i];
      var svl = new DeviceObject(sen);
      sens.push(svl);
    }
   return sens;
}





/********************************************************************************/
/*                                                                              */
/*      Sensor methods                                                          */
/*                                                                              */
/********************************************************************************/

function SensorObject(device,param)
{
   this.sensor_device = device;
   this.sensor_param = param;

   this.sensor_id = device.getId();
   this.sensor_name = param.getName();
   this.sensor_label = param.getLabel();
   this.sensor_desc = param.getDescription();
   if (this.sensor_name == device.getId()) this.sensor_name = device.getName();
   if (this.sensor_label == device.getId()) this.sensor_label = device.getLabel();
   if (this.sensor_description == device.getId()) this.sensor_description = device.getDescription();
   this.sensor_states = param.getValues();

   this.getId = function() { return this.sensor_id; }
   this.getName = function() { return this.sensor_name; }
   this.getLabel = function() { return this.sensor_label; }
   this.getDescription = function() { return this.sensor_desc; }
   this.getStates = function() { return this.sensor_states; }
   this.getParameter = function() { return this.sensor_param; }

}       // end of class SensorObject



function buildSensorSet(world)
{
   var sens = [];

   for (var i = 0; i < device_set.length; ++i) {
      var dev = device_set[i];
      var devsen = dev.getSensors();
      for (var j = 0; j < devsen.length; ++j) {
         sens.push(devsen[j]);
       }
    }

   return sens;
}


function findSensor(id)
{
   for (var i = 0; i < sensor_set.length; ++i) {
      var sen = sensor_set[i];
      if (sen.getId() == id || sen.getName() == id) return sen;
    }
   return null;
}




/********************************************************************************/
/*                                                                              */
/*      Entity methods                                                          */
/*                                                                              */
/********************************************************************************/

function EntityObject(dev)
{
   this.entity_device = dev;

   this.entity_parameters = [];
   var prms = dev.getParameters();
   for (var i = 0; i < prms.length; ++i) {
      var p = prms[i];
      if (p.isTarget()) this.entity_parameters.push(p);
    }

   this.getId = function() { return this.entity_device.getId(); }
   this.getName = function() { return this.entity_device.getName(); }
   this.getLabel = function() { return this.entity_device.getLabel(); }
   this.getDescription = function() { return this.entity_device.getDescription(); }
   this.getParameters = function() { return this.entity_parameters; }
   this.getTransitions = function() { return this.entity_device.getTransitions(); }

   this.getParameter = function(nm) {
      for (var i = 0; i < this.entity_parameters.length; ++i) {
         var prm = this.entity_parameters[i];
         if (prm.getId() == nm || prm.getName() == nm) return prm;
       }
      return null;
    }

}       // end of class EntityObject



function buildEntitySet(world)
{
   var ents = [];

   for (var i = 0; i < device_set.length; ++i) {
      var dev = device_set[i];
      if (dev.isTarget()) {
         var ent = new EntityObject(dev);
         ents.push(ent);
       }
    }

   return ents;
}



function findEntity(id)
{
   for (var i = 0; i < entity_set.length; ++i) {
      var ent = entity_set[i];
      if (ent.getId() == id || ent.getName() == id) return ent;
    }
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      XML/JSON access methods                                                 */
/*                                                                              */
/********************************************************************************/

function makeArray(obj)
{
   if (obj == null) return [];
   if (Array.isArray(obj)) return obj;
   return [obj];
}



function getAttr(x,name)
{
   // console.log("GETATTR",name,x);
   var attrs = x['@attributes'];
   if (attrs == undefined) return undefined;
   var rslt = attrs[name];
   return rslt;
}


function getAttrBool(x,name)
{
   var val = getAttr(x,name);
   if (val == null) return false;
   if (val == 'TRUE' || val == 'true' || val == '1' || val == 'YES' || val == 'y')
      return true;
   return false;
}


function getTextElement(x,name)
{
   var elt = x[name];
   if (elt == undefined || elt == null) return getAttr(x,name);
   if (elt['#cdata-section'] != undefined) return elt['#cdata-section'];
   return elt['#text'];
}


function getText(elt)
{
   if (elt == null) return null;
   if (elt['#cdata-section'] != undefined) return elt['#cdata-section'];
   return elt['#text'];
}



/********************************************************************************/
/*                                                                              */
/*      Utility methods                                                         */
/*                                                                              */
/********************************************************************************/

function extractCounter(id)
{
   if (id == undefined) return 0;
   var n = id.match(/[0-9]+/g);

   if (n == null) return 0;
   if (Array.isArray(n)) {
      if (n.length == 0) return 0;
      return Number(n[n.length-1]);
    }
   return Number(n);
}



function expand(html)
{
   var num = extractCounter(html.id)+1;

   var newhtml = html.cloneNode(true);
   expandFixIds(newhtml,num);

   return newhtml;
}


function expandFixIds(elt,num)
{
   if (elt.nodeType == 1) {
      if (elt.id != undefined && elt.id != null && elt.id != "")
         elt.id = fixIdString(elt.id,num);
      var nm = elt.getAttribute("name");
      if (nm != null) elt.setAttribute("name",fixIdString(nm,num));
      var cls = elt.getAttribute("class");
      if (cls != null) {
         var ncls = cls.replace("hasDatepicker","").trim();
         if (ncls != cls) elt.setAttribute("class",ncls);
       }
    }
   if (elt.hasChildNodes()) {
      for (var x = elt.firstChild; x != null; x = x.nextSibling) {
         expandFixIds(x,num);
       }
    }
}


function fixIdString(str,num)
{
   var y = String(num-1);
   var idx = str.lastIndexOf(y);
   if (idx > 0 && idx + y.length == str.length) {
      return str.substr(0,idx) + String(num);
    }
}



function forceshow(elt)
{
   elt.removeClass("starthidden");
   elt.show();
}


function decode(elt)
{
   if (elt == undefined) return "";

   return unescape(elt.replace(/[+]/g," "));
}



function timeString(val)
{
   if (val == null || val < 0) return "";
   var d = new Date();
   d.setTime(val);
   console.log("TIME",val,d);

   return d.toString("hh:mmtt");
}




function dateString(val)
{
   if (val == null || val < 0) return "";
   var d = new Date();
   d.setTime(val);
   console.log("DATE",val,d);

   return d.toString("MM/dd/yyyy");
}



/********************************************************************************/
/*                                                                              */
/*      Base64 encoding                                                         */
/*                                                                              */
/*      Source: http://www.webtoolkit.info                                      */
/*                                                                              */
/********************************************************************************/

var Base64 = {

   // private property
_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

   // public method for encoding
encode : function (input) {
   var output = "";
   var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
   var i = 0;

   input = Base64._utf8_encode(input);

   while (i < input.length) {

      chr1 = input.charCodeAt(i++);
      chr2 = input.charCodeAt(i++);
      chr3 = input.charCodeAt(i++);

      enc1 = chr1 >> 2;
      enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
      enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
      enc4 = chr3 & 63;

      if (isNaN(chr2)) {
         enc3 = enc4 = 64;
       } else if (isNaN(chr3)) {
            enc4 = 64;
          }

         output = output +
            this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
            this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);

    }

   return output;
},

   // public method for decoding
decode : function (input) {
   var output = "";
   var chr1, chr2, chr3;
   var enc1, enc2, enc3, enc4;
   var i = 0;

   input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

   while (i < input.length) {

      enc1 = this._keyStr.indexOf(input.charAt(i++));
      enc2 = this._keyStr.indexOf(input.charAt(i++));
      enc3 = this._keyStr.indexOf(input.charAt(i++));
      enc4 = this._keyStr.indexOf(input.charAt(i++));

      chr1 = (enc1 << 2) | (enc2 >> 4);
      chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
      chr3 = ((enc3 & 3) << 6) | enc4;

      output = output + String.fromCharCode(chr1);

      if (enc3 != 64) {
         output = output + String.fromCharCode(chr2);
       }
      if (enc4 != 64) {
         output = output + String.fromCharCode(chr3);
       }

    }

   output = Base64._utf8_decode(output);

   return output;
},

// private method for UTF-8 encoding
_utf8_encode : function (string) {
   string = string.replace(/\r\n/g,"\n");
   var utftext = "";

   for (var n = 0; n < string.length; n++) {

      var c = string.charCodeAt(n);

      if (c < 128) {
         utftext += String.fromCharCode(c);
       }
      else if((c > 127) && (c < 2048)) {
         utftext += String.fromCharCode((c >> 6) | 192);
         utftext += String.fromCharCode((c & 63) | 128);
       }
      else {
         utftext += String.fromCharCode((c >> 12) | 224);
         utftext += String.fromCharCode(((c >> 6) & 63) | 128);
         utftext += String.fromCharCode((c & 63) | 128);
       }

    }

   return utftext;
    },

   // private method for UTF-8 decoding
_utf8_decode : function (utftext) {
   var string = "";
   var i = 0;
   var c = c1 = c2 = 0;

   while ( i < utftext.length ) {

      c = utftext.charCodeAt(i);

      if (c < 128) {
         string += String.fromCharCode(c);
         i++;
       }
      else if((c > 191) && (c < 224)) {
         c2 = utftext.charCodeAt(i+1);
         string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
         i += 2;
       }
      else {
         c2 = utftext.charCodeAt(i+1);
         c3 = utftext.charCodeAt(i+2);
         string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
         i += 3;
       }

    }

   return string;
}

}



/********************************************************************************/
/*                                                                              */
/*      Hash methods                                                            */
/*                                                                              */
/********************************************************************************/

String.prototype.hashCode = function() {
   var hash = 0;
   if (this.length == 0) return hash;
   for (i = 0; i < this.length; i++) {
      char = this.charCodeAt(i);
      hash = ((hash<<5)-hash)+char;
      hash = hash & hash; // Convert to 32bit integer
    }
   return hash;
}



/* end of upodcode.js */
