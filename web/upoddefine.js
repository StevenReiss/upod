/********************************************************************************/
/*                                                                              */
/*              upodefine.js                                                    */
/*                                                                              */
/*      Code for managing rule definitions                                      */
/*                                                                              */
/********************************************************************************/




/********************************************************************************/
/*                                                                              */
/*      Variables                                                               */
/*                                                                              */
/********************************************************************************/

var picture_setup = new Object();




/********************************************************************************/
/*                                                                              */
/*      Condition area setup                                                    */
/*                                                                              */
/********************************************************************************/

function setupConditions(world)
{
   var opt0 = $("<option value='none' id='none1'>Always</option>");
   var optt = $("<option value='timesrule' id='timesrule1' selected='selected'>Scheduled Time ...</option>");
   var optc = $("<option value='calendarrule' id='calendarrule1'>Google Calendar Event ...</option>");
   var optr = $("<option value='timetrigger' id='timetrigrule1'>At a Scheduled Time ...</option>");
   var el = $('.ruleselect');
   el.empty();
   el.append(opt0);
   var cnds = makeArray(world.BASICCONDS.CONDITION);
   $.each(cnds,function(key,val) {
             var lbl = decode(getTextElement(val,"LABEL"))
             if (lbl != undefined) {
                var opt = $("<option></option>");
                opt.attr("value",getAttr(val,"NAME"));
                opt.text(lbl);
                el.append(opt);
              }
           });
   $.each(sensor_set,function(key,sen) {
             var prm = sen.getParameter();
             if (prm != null && (prm.getType() == 'REAL' || prm.getType() == 'INTEGER')) {
                var opt = $("<option></option>");
                opt.attr("value","rangerule$" + prm.getName() + "$" + sen.getId());
                opt.text(prm.getLabel() + " Range");
                el.append(opt);
              }
           });
   el.append(optt);
   el.append(optc);
   el.append(optr);

   initCondition(1,false,false,null);
}



function initCondition(i,havesked,havetrig,cond)
{
   var typ = 'none';
   if (!havesked) typ = 'timesrule';
   if (cond != null) {
      if (cond.getType() == 'TIME') typ = 'timesrule';
      else if (cond.getType() == 'GOOGLE') typ = 'calendarrule';
      else if (cond.getType() == 'RANGE') typ = 'rangerule';
      else if (cond.getType() == 'TIMETRIGGER') type = 'timetrigger';
      else typ = cond.getName();
    }
   $('#ruletype_' + i).val(typ);
   if (havetrig) {
      for (var i0 = 0; i0 < condition_set.length; ++i0) {
         var cond0 = condition_set[i0];
         if (cond0.isTrigger()) {
            var opt0 = $("#ruletype_" + i + " option[value='" + cond0.getName() + "']");
            opt0.remove();
          }
       }
      var opt1 = $("#ruletype_" + i + " option[value='timetrigger']");
      opt1.remove();
    }

   if (typ == 'timesrule' && cond != null) {
      $("#timedesc" + i).val(cond.getTimeDescription());
      $("#datefrom" + i).val(cond.getFromDate());
      $("#dateto" + i).val(cond.getToDate());
      $("#timefrom" + i).val(cond.getFromTime());
      $("#timeto" + i).val(cond.getToTime());
      $("#datefrom" + i).val(cond.getFromDate());
      var days = cond.getDays();
      if (days == null) days = "";
      ["mon","tue","wed","thu","fri","sat","sun"].map(
         function(day) {
            var d1 = day.toUpperCase();
            var fg = (days.search(d1) >= 0);
            $("#days" + day + i).prop("checked",fg);
          } );
      $("#repeattype" + i).val(cond.getInterval());
    }
   else {
      $("#timedesc" + i).val("");
      $("#datefrom" + i).val("");
      $("#dateto" + i).val("");
      $("#timefrom" + i).val("");
      $("#timeto" + i).val("");
      $("#timeselector" + i + " .daycheck").prop("checked",false);
      $("#repeattype" +i).val(0);
    }

   $("#caldesc" + i).val("");
   $("#calfielda" + i).val("none");
   $("#calfieldemptya" + i).val("EITHER");
   $("#calmatchtypea" + i).val("IGNORE");
   $("#calfieldmatcha" + i).val("");
   $("#calfieldb" + i).val("none");
   $("#calfieldemptyb" + i).val("EITHER");
   $("#calmatchtypeb" + i).val("IGNORE");
   $("#calfieldmatchb" + i).val("");
   $("#calfieldc" + i).val("none");
   $("#calfieldemptyc" + i).val("EITHER");
   $("#calmatchtypec" + i).val("IGNORE");
   $("#calfieldmatchc" + i).val("");
   $("#calfieldd" + i).val("none");
   $("#calfieldemptyd" + i).val("EITHER");
   $("#calmatchtyped" + i).val("IGNORE");
   $("#calfieldmatchd" + i).val("");
   $("#timetrigger" + i + " .timetrig0").val("0");
   $("#timetrigger" + i + " .timetrig1").val("*");

   if (typ == 'calendarrule' && cond != null) {
      $("#caldesc" + i).val(cond.getDescription());
      var ltr = ["a","b","c","d"];
      for (var j = 0; j < cond.getCalFieldCount(); ++j) {
         var fld = cond.getCalField(j);
         $("#calfield" + ltr[j] + i).val(fld.getName());
         $("#calfieldempty" + ltr[j] + i).val(fld.getNullType());
         $("#calmatchtype" + ltr[j] + i).val(fld.getMatchType());
         $("#calfieldmatch" + ltr[j] + i).val(fld.getMatchValue());
       }
    }

   $("#rangemin" + i).val("");
   $("#rangemin" + i).attr("min","");
   $("#rangemin" + i).attr("max","");
   $("#rangemax" + i).val("");
   $("#rangemax" + i).attr("min","");
   $("#rangemax" + i).attr("max","");

   if (typ == 'rangerule' && cond != null) {
      var minv = cond.getMinValue();
      var maxv = cond.getMaxValue();
      if (minv != maxv) {
         $("#rangemin" + i).attr("min",minv);
         $("#rangemax" + i).attr("min",minv);
         $("#rangemin" + i).attr("max",maxv);
         $("#rangemax" + i).attr("max",maxv);
       }
      var lowv = cond.getLowValue();
      if (lowv != null) $("#rangemin" + i).val(lowv);
      var highv = cond.getHighValue();
      if (highv != null) $("#rangemax" + i).val(highv);
    }

   if (typ == 'timetrigger' && cond != null) {
      $("#timetrigmin" + i).val(cond.getTimeTrigger("MINUTE"));
      $("#timetrighour" + i).val(cond.getTimeTrigger("HOUR"));
      $("#timetrigday" + i).val(cond.getTimeTrigger("DAY"));
      $("#timetrigmonth" + i).val(cond.getTimeTrigger("MONTH"));
      $("#timetrigweek" + i).val(cond.getTimeTrigger("WEEKDAY"));
    }
}




/********************************************************************************/
/*                                                                              */
/*      Action area setup                                                       */
/*                                                                              */
/********************************************************************************/

var action_map = new Object();


function setupActions(world)
{
   return;

   var parea = $('#actionparams');

   var el = $('.actionselect');
   el.empty();

   for (var i = 0; i < entity_set.length; ++i) {
      var ent = entity_set[i];
      var entuid = ent.getId();
      var entlbl = ent.getLabel();

      setupNoneTransition(el,entuid,entlbl);

      var trns = ent.getTransitions();
      for (var j = 0; j < trns.length; ++j) {
         var trn = trns[j];
         var id = entuid + "_" + trn.getName();
         action_map[id] = new Object();
         var div = setupTransitionArea(entuid,entlbl,id,trn);
         setupTransitionOption(el,entuid,entlbl,id,trn);
         parea.append(div);
       }
    }
}




function setupNoneTransition(el,entuid,entlbl)
{
    var opt = $("<option class='noneoption'></option>");
    var id = entuid + "_NONE";
    opt.attr("value",id);
    opt.text("Don't change");
    el.append(opt);
    action_map[id] = new Object();
    action_map[id].entuid = entuid;
    action_map[id].transname = null;
}



function setupTransitionOption(el,entuid,entlbl,id,trns)
{
   var opt = $("<option></option>");
   var tnm = trns.getName();
   var lbl = trns.getLabel();
   opt.attr("value",id);
   opt.text(lbl);
   el.append(opt);
   action_map[id].entuid = entuid;
   action_map[id].transname = tnm;
}



function setupTransitionArea(entuid,entlbl,id,trns)
{
   action_map[id].parameters = new Object();

   var paramid = "parms_" + id;
   var div = $("<div class='actionoptions starthidden'></div>");
   div.attr("id",paramid);
   var prms = trns.getParameters();
   var tnm = trns.getName();
   for (var i = 0; i < prms.length; ++i) {
      var prm = prms[i];
      var ptyp = prm.getType();
      var pnam = prm.getName();
      var plbl = prm.getLabel();
      var ndiv = $("<div class='actionparam'></div>");
      var pid = 'actionparam_' + i;
      var x = null;
      action_map[id].parameters[pid] = new Object() ;
      action_map[id].parameters[pid].type = ptyp;
      action_map[id].parameters[pid].name = pnam;

      switch (ptyp) {
         case 'STRING' :
            break;
         case 'BOOLEAN' :
            break;
         case 'INTEGER' :
            var numimp = "<input type='NUMBER' id='" + pid + "'";
            numimp +=  " min='" + prm.getMinValue() + "' max='" + prm.getMaxValue() + "'";
            numimp += "/>";
            x = $("<span class='paramlabel'>" + pnam + ": </span>" + numimp);
            break;
         case 'REAL' :
            var numimp = "<input type='NUMBER' id='" + pid + "'";
            numimp += " min='" + prm.getMinValue() + "' max='" + prm.getMaxValue() + "'";
            numimp += "/>";
            x = $("<span class='paramlabel'>" + pnam + ": </span>" + numimp);
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
         case 'COLOR' :
            var colimp = "<input type='COLOR' id='" + pid + "'/>";
            x = $("<span class='paramlabel'>" + pnam + ": </span>" + colimp);
            break;
         case 'PICTURE' :
            x = $("<iframe id='"  + pid + "' src='svg/svg-edit/picture-editor.html' " +
                     "class='pictureeditor' onload='initPicture(" + pid + ",\"" +
                     pnam + "\",\"" + tnm + "\",\"" +
                     entuid + "\");' >" +
                     "</iframe>");
            break;
         case 'EVENT_MATCH' :
            break;
       }
      if (x != null) ndiv.append(x);
      div.append(ndiv);
    }
   return div;
}


var ctr = 0;


function initPicture(pid,pnam,tnm,eid)
{
   var xid = pid.id;
   if (xid == undefined) xid = pid.name;
   if (xid == undefined) xid = pid;

   var y = pid;
   y = $("#" + pid)[0].contentWindow;
   if (y == undefined) {
      y = $(pid)[0].window;             // handle chrome
    }

   var svgcan = y.svgCanvas;
   var svgedit = y.svgEditor;

   if (eid != undefined) {
      y.upodEntity = eid;
      y.upodTransition = tnm;
      y.upodParameter = pnam;
    }

   var cnt = picture_setup[xid];
   if (cnt == undefined || cnt == "OK") {
      cnt = "<svg width='640' height='480' xmlns='http://www.w3.org/2000/svg'>" +
         "<g>  <title>Layer 1</title> </g></svg>";
    }

   svgedit.loadFromString(cnt);
   picture_setup["#" + xid] = "OK";
}



function clearPicture(pid)
{
   var xid = pid.id;

   var y = pid;
   y = $(pid)[0].contentWindow;
   if (y == undefined) {
      y = $(pid)[0].window;             // handle chrome
    }

   var svgedit = y.svgEditor;

   cnt = "<svg width='640' height='480' xmlns='http://www.w3.org/2000/svg'>" +
      "<g>  <title>Layer 1</title> </g></svg>";

   svgedit.loadFromString(cnt);
   picture_setup[xid] = "OK";
}



function initAction(acts)
{
   for (var i = 0; i < acts.length; ++i) {
      var act = acts[i];
      if (act.getLabel() != null && act.getLabel() != '') {
         $("#actionlabel").val(act.getLabel());
         break;
       }
    }

   $(".actionedittype").val('NONE');

   for (i = 0; i < acts.length; ++i) {
      act = acts[i];
      var entid = act.getEntityId();
      var trnsnm = act.getTransitionName();
      var aid;
      if (trnsnm == null) aid = "NONE";
      else aid = trnsnm;
      $("#actionedittype_" + entid).val(aid);
      var t1 = $("#actionedittype_" + entid);
      setActionType($('#actionedittype_' + entid)[0]);

      // set parameters for rule
      var ent = findEntity(entid);
      var parms = act.getParameters();
      for (var parm = 0; parm < parms.length; ++parm) {
         var pobj = parms[parm];
         var xpid = "#actionparam_" + entid + "_" + trnsnm + "_" + pobj.getName();
         var pval = pobj.getValue();
         setParamValue(xpid,pobj,pval);
       }
    }
}


function setParamValue(xpid,pobj,val)
{
   var ptyp = pobj.getType();

   switch (ptyp) {
      case 'STRING' :
      case 'INTEGER' :
      case 'REAL' :
      case 'COLOR' :
         $(xpid).val(val);
         break;
      case 'BOOLEAN' :
      case 'SET' :
      case 'ENUM' :
         $(xpid).val(val);
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
      case 'FILE' :
         break;
      case 'PICTURE' :
         if (picture_setup[xpid] == undefined) {
            picture_setup[xpid] = val;
          }
         else {
            var y = $(xpid)[0].contentWindow;
            if (y == undefined) y = $(xpid)[0].window;
            var svgedit = y.svgEditor;
            if (svgedit == null) {
               picture_setup[xpid] = val;
             }
            else {
               svgedit.loadFromString(val);
             }
          }
         break;
      case 'EVENT_MATCH' :
         break;
    }
}



/********************************************************************************/
/*                                                                              */
/*      Rule context methods                                                    */
/*                                                                              */
/********************************************************************************/

function setupRuleContext(world)
{
   var opt0 = $("<option value='*' id='none1'>All Devices</option>");
   var el = $('#rulecatdevices');
   el.empty();
   el.append(opt0);

   for (var i = 0; i < entity_set.length; ++i) {
      var ent = entity_set[i];
      var opt = $("<option></option>");
      opt.attr("value",ent.getId());
      opt.text(ent.getLabel());
      el.append(opt);
    }
   el.val(0);

   opt0 = $("<option value='*' id='none1'>All Sensors</option>");
   el = $('#rulecatsensors');
   el.empty();
   el.append(opt0);

   for (var i = 0; i < sensor_set.length; ++i) {
      var sen = sensor_set[i];
      var opt = $("<option></option>");
      opt.attr("value",sen.getId());
      opt.text(sen.getLabel());
      el.append(opt);
    }
   el.val(0);

   opt0 = $("<option value='*' id='none1'>All Conditions</option>");
   el = $('#rulecatconds');
   el.empty();
   el.append(opt0);

   for (var i = 0; i < condition_set.length; ++i) {
      var cnd = condition_set[i];
      var opt = $("<option></option>");
      opt.attr("value",cnd.getName());
      opt.text(cnd.getLabel());
      el.append(opt);
    }
   el.val(0);

}




/********************************************************************************/
/*                                                                              */
/*      Rule definition methods                                                 */
/*                                                                              */
/********************************************************************************/

function setRuleType(sel)
{
   var num = extractCounter(sel.id);

   var value = sel.options[sel.selectedIndex].value;
   if (value == 'timesrule') {
      forceshow($('#timeselector' + num));
      $('.dateregion').datepicker( { } );
      $('.timeregion').timepicker({ });
    }
   else $('#timeselector' + num).hide();

   if (value == 'calendarrule') forceshow($('#calendarselector' + num));
   else $('#calendarselector' + num).hide();

   if (value.startsWith('rangerule$')) forceshow($('#rangeselector' + num));
   else $('#rangeselector' + num).hide();

   if (value == 'timetrigger') forceshow($('#timetrigger' + num));
   else $('#timetrigger' + num).hide();

   checkRuleValid();
}



function expandRule(and)
{
   var html = and.previousSibling;
   while (html != null && html.nodeType != 1) {
      html = html.previousSibling;
    }

   var newhtml = expand(html);
   var newand = $("<span class='andstring'>AND</span>")[0];

   var par = and.parentNode;
   par.insertBefore(newand,and);
   par.insertBefore(newhtml,and);

   var elt = $('.ruleselect');
   var ct = elt.length;
   var sel = elt[ct-1];

   $('.dateregion').datepicker({ });
   $('.timeregion').timepicker({ });

   var havetime = false;
   var havetrig = false;
   for (var i = 1; i < ct; ++i) {
      var val = $("#ruletype_" + i).val();
      if (val == 'timesrule') havetime = true;
      if (val == 'timetrigger') havetrig = true;
      for (var i0 = 0; i0 < condition_set.length; ++i0) {
         var cond = condition_set[i0];
         if (cond.getName() == val) {
            if (cond.isTrigger()) havetrig = true;
            break;
          }
       }
    }
   initCondition(i,havetime,havetrig,null);

   setRuleType(sel);

   checkRuleValid();
}



/********************************************************************************/
/*                                                                              */
/*      Action definition methds                                                */
/*                                                                              */
/********************************************************************************/

function setActionType(sel)
{
   var t2 = $(sel).parent();
   var t1 = t2.find(".actionoptions");
   t1.hide();

   var eid = sel.id;
   eid = eid.replace('actionedittype_','parms_');
   var value = sel.options[sel.selectedIndex].value;
   var id = "#" + eid + "_" + value;

   var x = $(id);

   forceshow($(id));
}




/********************************************************************************/
/*                                                                              */
/*      Clear methods                                                           */
/*                                                                              */
/********************************************************************************/

function clearRuleArea()
{
   $("#rulelabel").val("");

   var t1 = $(".newrule .andstring");
   t1.remove();
   t1 = $(".newrule .singlerule :gt(0)");
   t1.remove();
   initCondition(1,false,false,null);

   $("#actionlabel").val("");

   $('.actionedittype').val('NONE');
   $('.actionoptoins').hide();
   $('.numbereditor').val(0);
   $(".pictureeditor").each(function (idx,elt) {
                               var id = elt.id;
                               clearPicture("#" + id);
                             });
}




/********************************************************************************/
/*                                                                              */
/*      New Rule methods                                                        */
/*                                                                              */
/********************************************************************************/

function editRule()
{
   var old = $("#ruleeditid").val();
   addNewRule(old);
}


function addNewRule(oldruleid)
{
   var obj = getRuleValues(true);

   var xml = "<ADD_RULE";
   if (oldruleid != undefined && oldruleid != "") {
      xml += " REPLACE='" + oldruleid + "'";
    }
   xml += ">"

   xml += "<RULE PRIORITY='" + getNewPriority() + "'>";
   var lbl = $("#rulelabel").val();
   if (lbl != "") xml += "<LABEL>" + escape(lbl) + "</LABEL>";

   xml += addRuleCondition(obj);
   xml += addRuleAction(obj);
   xml += "</RULE></ADD_RULE>";

   loadJson(xml,function(jresp) {
               finishNewRule();
               console.log("RULE ADDED");
               loadProgram(function () { setupRules(); });
             });

   // console.log("RULE",xml);
}


function cancelNewRule()
{
   finishNewRule();
}



function getRuleValues(pics)
{
   var inps = $("#newruleform :input");
   var obj = new Object();
   inps.each(function () {
              if (this.type == 'checkbox') {
                 if (!this.checked) return;
               }
              obj[this.id] = $(this).val();
            });

   var eds = $("#newruleform .pictureeditor");
   eds.each(function () {
               var id = this.id;
               var rslt = null;
               if (pics) {
                  var y = $(this)[0].contentWindow;
                  var svgcan = y.svgCanvas;
                  var txt = svgcan.svgCanvasToString();
                  rslt = txt;
                }
               else rslt = true;
               obj[id] = rslt;
             });

   return obj;
}




function addRuleCondition(args)
{
   var xml = "";

   var nrule = 0;
   for (var i = 1; i < 100; ++i) {
      var id = args['ruletype_' + i];
      if (id == undefined) break;
      if (id == 'none') continue;
      ++nrule;
    }
   if (nrule == 0) return;
   if (nrule > 1) {
      xml += "<CONDITION TYPE='AND'>";
    }
   for (var i = 1; i < 100; ++i) {
      var id = args['ruletype_' + i];
      if (id == undefined) break;
      if (id == 'none') continue;
      if (id == 'timesrule') {
         xml += "<CONDITION TYPE='TIME' >";
         xml += "<CALEVENT ";
         xml += addDateValue("FROMDATE",args["datefrom" + i]);
         xml += addDateValue("TODATE",args["dateto" + i]);
         xml += addDateValue("FROMTIME",args["timefrom" + i]);
         xml += addDateValue("TOTIME",args["timeto" + i]);
         xml += " INTERVAL='" + args["repeattype" + i] + "'";
         var days = "";
         for (var x in { "mon": 1, "tue": 2, "wed": 3, "thu": 4, "fri": 5, "sat":6, "sun":7 }) {
            if (args["days" + x + i]) days += "," + x;
          }
         if (days != "") xml += " DAYS='" + days.substr(1).toUpperCase() + "'";
         xml += " />";
         var desc = args["timedesc" + i];
         if (desc != null && desc != "") {
            xml += "<LABEL>" + escape(desc) + "</LABEL>";
          }
         xml += "</CONDITION>";
       }
      else if (id == 'calendarrule') {
         xml += "<CONDITION TYPE='GOOGLE' >";
         var desc = args['caldesc' + i];
         xml += "<LABEL>" + escape(desc) + "</LABEL>";
         ['a','b','c','d'].map( function(j) {
            var fld = args['calfield' + j + i];
            if (fld != 'none') {
               xml += "<FIELD NAME=' " + fld + "'";
               var emp = args['calfieldemtpy' + j + i];
               xml += " NULL='" + emp + "'";
               var mat = args['calmatchtype' + j + i];
               xml += " MATCH='" + mat + "'";
               var txt = args['calfieldmatch' + j + i];
               xml += " MATCHTEXT='" + txt + "'";
               xml += " />";
             }
                                 } );
         xml += "</CONDITION>";
       }
      else if (id.startsWith("rangerule$")) {
         var idx = id.lastIndexOf("$");
         var senid = id.substring(idx+1);
         var prmid = id.substring(10,idx);
         xml += "<CONDITION TYPE='RANGE'";
         xml += " PARAMETER='" + prmid + "'";
         var lowv = args['rangemin' + i];
         var highv = args['rangemax' + i];
         if (lowv != "" && lowv != null) xml += "LOW='" + lowv + "'";
         if (highv != "" && highv != null) xml += "HIGH='" + highv + "'";
         xml += "><DEVICE ID='" + senid + "'/>";
         xml += "</CONDITION>";
       }
      else if (id == 'timetrigger') {
         xml += "<CONDITYPE TYPE='TIMETRIGGER' ";
         xml += "MINUTE='" + getTimeValue(args,i,"min") + "' ";
         xml += "HOUR='" + getTimeValue(args,"hour") + "' ";
         xml += "DAY='" + getTimeValue(args,"day") + "' ";
         xml += "MONTH='" + getTimeValue(args,i,"month") + "' ";
         xml += "WEEKDAY='" + getTimeValue(args,i,"week") + "' ";
         var desc = args['timetrigdesc' + i];
         xml += "<LABEL>" + escape(desc) + "</LABEL>";
         xml += "</CONDITION>";
       }
      else {
         xml += "<CONDITION TYPE='PRIM' NAME='" + id + "' />";
       }
    }
   if (nrule > 1) {
      xml += "</CONDITION>";
    }

   return xml;
}


function getTimeValue(args,i,elt)
{
   return escape(args['timetrig' + elt + i]);
}


function addDateValue(tag,arg)
{
   if (arg == "") return "";

   var d = Date.parse(arg);
   if (isNaN(d)) return "";

   console.log("DATESET",arg,d);

   return " " + tag + "='" + d.getTime() + "'";
}



function addTimeValue(tag,arg)
{
   if (arg == "") return "";

   var d = Date.parseExact(arg,"hh:mmtt");
   if (isNaN(d)) return "";

   console.log("DATESET",arg,d);

   return " " + tag + "='" + d.getTime() + "'";
}




function addRuleAction(args)
{
   var lbl = $('#actionlabel').val();
   var xml = "<ACTIONS>";

   for (var i = 0; i < entity_set.length; ++i) {
      var ent = entity_set[i];
      var x = $("#actionedittype_" + ent.getId()).val();
      if (x == 'NONE') continue;
      xml += "<ACTION>";
      if (lbl != null && lbl != '') {
         xml += "<LABEL>" + escape(lbl) + "</LABEL>";
         lbl = null;
       }
      xml += "<DEVICE ID='" + ent.getId() + "' NAME='" + ent.getName() + "' />";
      xml += "<TRANSITION NAME='" + x + "' />";
      xml += "<PARAMETERS>";
      var tag = "actionparam_" + ent.getId() + "_" + x;
      var len = tag.length;
      $("." + tag).each(
         function (idx,elt) {
            var id = elt.id;
            var pnm = id.substring(len+1);
            var val = args[id];
            xml += "<PARAMETER NAME='" + pnm + "'>";
            xml += escape(val);
            xml += "</PARAMETER>";
          } );
      xml += "</PARAMETERS>";
      xml += "</ACTION>";
    }
   xml += "</ACTIONS>";

   return xml;
}




/********************************************************************************/
/*                                                                              */
/*      Rule validation methods                                                 */
/*                                                                              */
/********************************************************************************/

// checkRuleValid is defined locally.  It should call performValidityCheck and
// take any local actions


function performValidityCheck()
{
   var vld = validateRule();

   if (vld) {
      $('#rulesubmit').removeAttr('disabled');
      $('#ruleedit').removeAttr('disabled');
    }
   else {
      $('#rulesubmit').attr('disabled','disabled');
      $('#ruleedit').attr('disabled','disabled');
    }
}


var doing_validate = false;


function validateRule()
{
   if (doing_validate) return true;
   doing_validate = true;

   var rslt = true;

   var data = getRuleValues(false);

   rslt &= validateConditions(data,false);

   rslt &= (data.rulelabel != "");

   // TODO: validate action

   doing_validate = false;

   return rslt;
}


function validateConditions(data,allowzero)
{
   if (allowzero == undefined) allowzero = false;

   if (data == null) data = getRuleValues(false);

   var ct = 0;
   for (var i = 1; i < 1000; ++i) {
      var typ = data['ruletype_' + i];
      if (typ == undefined) break;
      if (typ == 'none') continue;
      ++ct;
      if (typ == 'timesrule') {
         var dt = data['datefrom' + i];
         var dt1 = data['dateto' + i];
         if (dt != "" && isNaN(Date.parse(dt))) return false;
         if (dt1 != "" && isNaN(Date.parse(dt1))) return false;

         var tt = data['timefrom' + i];
         var tt1 = data['timeto' + i];
         if (tt != null && isNaN(Date.parse(tt))) return false;
         if (tt1 != null && isNaN(Date.parse(tt1))) return false;

         if (dt == "" && dt1 == "" && tt == "" && tt1 == "") {
            var ctr = 0;
            for (var x in { "mon": 1, "tue": 2, "wed": 3, "thu": 4, "fri": 5, "sat":6, "sun":7 }) {
               if (data["days" + x + i]) ++ctr;
             }
            if (ctr == 0 || ctr == 7) return false;
          }
       }
      else if (typ == 'calendarrule') {
         var fct = 0;
         var vld = true;
         ['a','b','c','d'].map( function(j) {
                           var fld = data['calfield' + j + i];
                           if (fld == 'none') {
                              resetFields(j + i);
                              return;
                            }
                           ++fct;
                           var nl = data['calfieldempty'+ j + i];
                           if (nl == 'NULL') {
                              $("#calfieldmatch" + j + i).val("");
                              return;
                            }
                           var ml = data['calfieldmatchtype' + j + i];
                           if (ml != 'IGNORE') {
                              var vl = data['calfieldmatch' + j + i];
                              if (vl == "") vld = false;
                            }
                           else {
                              $("#calfieldmatch" + j + i).val("");
                            }
                         } );
         if (fct == 0 || !vld) return false;
       }
      else if (typ == 'timetrigger') {
         // validate time trigger here
       }
    }

   if (ct == 0 && !allowzero) return false;

   return true;
}


function resetFields(x)
{
   $("#calfieldempty" + x).val("EITHER");
   $("#calmatchtype" + x).val("MATCH");
   $("#calfieldmatch" + x).val("");
}




/* end of upodefine.js */































































