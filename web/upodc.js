/********************************************************************************/
/*                                                                              */
/*              upodc.js                                                        */
/*                                                                              */
/*      Code to manipulate C-prototype smartsign web page                       */
/*                                                                              */
/********************************************************************************/



/********************************************************************************/
/*                                                                              */
/*      Global variables                                                        */
/*                                                                              */
/********************************************************************************/

var     our_world = null;
var     sensor_state = new Object();
var     set_count = 0;

var     ID_SEP = "__SEP__";




/********************************************************************************/
/*                                                                              */
/*      Setup on load                                                           */
/*                                                                              */
/********************************************************************************/

$(function() {
        loadDescription(setupPage);
});


$(window).unload(function () {
                    if (our_world != null) {
                       loadJson("<REMOVE_WORLD WORLD='" + our_world + "' />");
                       our_world = null;
                     }
                  } );




/********************************************************************************/
/*                                                                              */
/*      Handle setup after world is loaded                                      */
/*                                                                              */
/********************************************************************************/


function setupPage(world)
{
   setupCommon(world);

   // Set up Basic Sensors
   setupSensorSelection();

   setupResultArea();

   loadJson("<CREATE_WORLD />",function(wid) {
               our_world = getAttr(wid,"ID");
               var tim = getAttr(wid,"TIME");
               var dat = new Date();
               if (tim != null) dat = new Date(tim*1);

               // console.log("WORLD",our_world);
               ++set_count;
               setHypotheticalDefaults(wid);
               updateResult(wid);
               $("#curdate").datepicker("setDate",dat);
               $("#curtime").timepicker("setTime",dat);
               setHypotheticalWorldTime();
               --set_count;
               setHypotheticalWorldSensors();
               handleSet();
             });
}



/********************************************************************************/
/*                                                                              */
/*      Setup sensor selections                                                 */
/*                                                                              */
/********************************************************************************/

function setupSensorSelection()
{
   return;

   var el = $('#sensorlist');
   el.empty();

   for (var i = 0; i < sensor_set.length; ++i) {
      var sen = sensor_set[i];
      var lbl = sen.getLabel();
      var prm = sen.getParameter();
      var typ = prm.getType();
      var tag = "sen_" + sen.getId() + ID_SEP + sen.getName();

      var txt = "<tr><td><label for='" + tag + "'>" + lbl + "</label>";
      txt += "</td><td>";

      if (typ == "SET" || typ == "BOOLEAN") {
         var sts = sen.getStates();
         txt += "<select name='" + tag + "' id='" + tag + "' class='sensorstate' ";
         txt += "onchange='updateSensors();'>";
         for (var j = 0; j < sts.length; ++j) {
            var state = sts[j];
            txt += "<option value='" + state + "'";
            if (j == 0) txt += " selected='selected'";
            txt += ">" + state + "</option>";
          }
         txt += "</select>";
       }
      else if (typ == "REAL" || typ == "INTEGER") {
         txt += "<input type='number' name='" + tag + "' id='" + tag + "' class='sensorinput' ";
         txt += "onchange='updateSensors();'";
         if (prm.getMinValue() != null) {
            txt += " min='" + prm.getMinValue() + "'";
          }
         if (prm.getMaxValue() != null) {
            txt += " max='" + prm.getMaxValue() + "'";
          }
         txt += "/>";
       }
      else if (typ == "STRING") {
         txt += "<input typ='string' name='" + tag + "' id='" + tag + "' class='sensorinput' ";
         txt += "onchange='updateSensors();'";
         txt += "/>";
       }

      txt += "</td></tr>";
      var ht = $(txt);
      el.append(ht);
    }
}




/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

function updateDateTime()
{
   var date = $('#curdate').val();
   var time = $('#curtime').val();
   if (date == "" || time == "") return;

   setHypotheticalWorldTime();
}



function updateSensors()
{
   setHypotheticalWorldSensors();
}


/********************************************************************************/
/*                                                                              */
/*      Methods to update events and result                                     */
/*                                                                              */
/********************************************************************************/

function setHypotheticalWorldTime()
{
   if (our_world == null) return;

   var date = $('#curdate').val();
   var time = $('#curtime').val();
   if (date == "" || time == "") return;

   var dt = date + " " + time;
   var d = Date.parse(dt);

   ++set_count;
   loadJson("<SET_TIME WORLD='" + our_world + "' TIME='" + d.getTime() + "' />",handleSet);
}


function setHypotheticalDefaults(world)
{
   var ents = makeArray(world["DEVICE"]);
   for (var i = 0; i < ents.length; ++i) {
      var eid = getAttr(ents[i],"ID");
      var sen = findSensor(eid);
      if (sen == null) continue;
      var prms = makeArray(ents[i]["PARAMETER"]);
      for (var j = 0; j < prms.length; ++j) {
         var pid = getAttr(prms[j],"NAME");
         var prm = sen.getParameter();
         if (prm.getId() == pid || prm.getName() == pid) {
            var val = getText(prms[j]["CURRENT"]);
            if (val != null) {
               var ptyp = prm.getType();
               var id = "#sen_" + sen.getId() + ID_SEP + prm.getName();
               sensor_state[sen.getId() + ID_SEP + sen.getName()] = val;
               setParamValue(id,prm,val);
             }
          }
       }
    }
}


function setHypotheticalWorldSensors()
{
   if (our_world == null) return;

   ++set_count;
   for (var i = 0; i < sensor_set.length; ++i) {
      var sen = sensor_set[i];
      var id = sen.getId();
      var prm = sen.getParameter();
      var sid = sen.getId() + ID_SEP + prm.getName();

      var key = "#sen_" + sid;

      var val = null;
      switch (sen.getParameter().getType()) {
         case "BOOLEAN" :
         case "SET" :
            // val = $(key + " option:selected").val();
            val = $(key).val();
            break;
         default :
         case "INTEGER" :
         case "REAL" :
         case "STRING" :
            val = $(key).val();
            break;
        }
      if (val == null) continue;

      if (sensor_state[sid] == val) continue;
      sensor_state[sid] = val;
      var cmd = "<SET_SENSOR WORLD='" + our_world + "' STATE='" + val + "'";
      var prm = sen.getParameter();
      cmd += " PARAM='" + prm.getName() + "'";
      cmd += ">";
      cmd += "<DEVICE ID='" + id + "' />";
      cmd += "</SET_SENSOR>";
      ++set_count;
      loadJson(cmd,handleSet);
    }
   handleSet();
}



function handleSet()
{
   --set_count;
   if (set_count > 0) return;
   set_count = 0;

   var date = $('#curdate').val();
   var time = $('#curtime').val();
   if (date == "" || time == "") return;
   if (our_world == null) return;

   loadJson("<LIST WORLD='" + our_world + "' />",updateResult);
}



/********************************************************************************/
/*                                                                              */
/*      Result area methods                                                     */
/*                                                                              */
/********************************************************************************/

function setupResultArea()
{
   return;

   var html = "";

   var ctr = 0;
   for (var i = 0; i < entity_set.length; ++i) {
      var ent = entity_set[i];
      var prms = ent.getParameters();
      for (var j = 0; j < prms.length; ++j) {
         var prm = prms[j];
         var ptyp = prm.getType();
         var id = "RESULT_" + ent.getId() + ID_SEP + prm.getId();
         html += "<div>";
         if (i > 0 || j > 0) html += "<br>";
         switch (ptyp) {
            case 'STRING' :
               break;
            case 'BOOLEAN' :
               break;
            case 'INTEGER' :
               break;
            case 'REAL' :
               break;
            case 'TIME' :
               break;
            case 'DATE' :
               break;
            case 'DATETIME' :
               break;
            case 'TIME_RANGE' :
               break;
            case 'DATE_RANGE' :
               break;
            case 'DATETIME_RANGE' :
               break;
            case 'ENUM' :
               break;
            case 'FILE' :
               break;
            case 'PICTURE' :
               html += "<iframe id='"  + id + "' src='svg/svg-edit/picture-editor.html' " +
                  "class='pictureeditor' onload='initPicture(" + id +
                  ",\"" + prm.getName() + "\",null,\"" + ent.getId() +
                  "\");' >" +
                  "</iframe>";
               break;
            case 'EVENT_MATCH' :
               break;
          }
         html += "</div>";
       }
    }

   $("#setresult").html(html);
}


function updateResult(world)
{
   var ents = makeArray(world["DEVICE"]);
   for (var i = 0; i < ents.length; ++i) {
      var eid = getAttr(ents[i],"ID");
      var ent = findEntity(eid);
      if (ent == null) continue;
      var prms = makeArray(ents[i]["PARAMETER"]);
      for (var j = 0; j < prms.length; ++j) {
         var pid = getAttr(prms[j],"NAME");
         var prm = ent.getParameter(pid);
         if (prm == null) continue;
         var val = getText(prms[j]["CURRENT"]);
         var ptyp = prm.getType();
         var id = "#RESULT_" + ent.getId() + ID_SEP + prm.getId();
         setParamValue(id,prm,val);
       }
    }
}


/********************************************************************************/
/*                                                                              */
/*      Create Rule command                                                     */
/*                                                                              */
/********************************************************************************/

function createNewSetRule()
{
   var obj = loadNewRuleValues();
   var xml = "<DEDUCE_RULE WORLD='" + our_world + "'>";
   xml += "<WHEN DATE='" + obj.curdate + "' TIME='" + obj.curtime + "' />";
   for (var id in obj) {
      var typ = null;
      var xid = null;
      var eid = null;
      if (id.substr(0,7) == "RESULT_") {
         typ = "RESULT";
         xid = id.substr(7);
         var idx = xid.indexOf(ID_SEP);
         eid = xid.substr(0,idx);
         xid = xid.substr(idx+ID_SEP.length);
         if (param_map[xid] != undefined) xid = param_map[xid];
       }
      else if (id.substr(0,4) == "sen_") {
         typ = "SENSOR";
         xid = id.substr(4);
         var idx = xid.indexOf(ID_SEP);
         eid = xid.substr(0,idx);
         xid = xid.substr(idx+ID_SEP.length);
         if (param_map[xid] != undefined) xid = param_map[xid];
       }
      else continue;

      xml += "<PARAM NAME='" + xid + "' TYPE='" + typ + "'";
      if (eid != null) xml += " DEVICE='" + eid + "'";
      xml += ">";
      xml += "<![CDATA[" + obj[id] + "]]>";
      xml += "</PARAM>";
    }
   xml += "</DEDUCE_RULE>";
   loadJson(xml);
}



function loadNewRuleValues()
{
   var inps = $("#setruleform :input");
   var obj = new Object();
   inps.each(function () {
              if (this.type == 'checkbox') {
                 if (!this.checked) return;
               }
              obj[this.name] = $(this).val();
            });

   var eds = $("#setruleform .pictureeditor");
   eds.each(function () {
               var id = this.id;
               var y = $(this)[0].contentWindow;
               var svgcan = y.svgCanvas;
               var txt = svgcan.svgCanvasToString();
               obj[id] = txt;
             });

   return obj;
}






/* end of smartsignc.js */

















































