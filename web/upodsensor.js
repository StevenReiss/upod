/********************************************************************************/
/*                                                                              */
/*              upodsensor.js                                                   */
/*                                                                              */
/*      Methods for handling sensor creation and editing                        */
/*                                                                              */
/********************************************************************************/




/********************************************************************************/
/*                                                                              */
/*      Startup                                                                 */
/*                                                                              */
/********************************************************************************/

$(function() {
     checkDurationSensorValid();
   });




/********************************************************************************/
/*                                                                              */
/*      Duration sensor                                                         */
/*                                                                              */
/********************************************************************************/

function checkDurationSensorValid()
{
   var fg = getDurationValues() != null;

   if (fg) $("#durationdefine").removeAttr('disabled');
   else $("#durationdefine").attr('disabled','disabled');
}




function addDurationSensor()
{
   var formdata = getDurationValues();
   if (formdata == null) return;

   var url = "/rest/sensor/createtimed";

   $.ajax({ action: "test",
            type: "POST",
            cache: false,
            url: url,
            data: formdata,
            dataType: 'json',
            error: afterError,
            success: afterCreate} );
}




function getDurationValues()
{
   var lbl = $("#durationsensorlabel").val();
   if (lbl == "" || lbl == null) return null;

   var cnd = $("#durationsensorcond").val();
   if (cnd == "" || cnd == "NONE" || cnd == null) return null;

   var start = $("#durationstart").val();
   var end = $("#durationend").val();
   if (start == "" || end == "") return null;
   if (start <= 0 && end <= 0) return null;

   if (start <= 0 || start == "" || start == null) start = 0;
   if (end <= 0 || end == "" || end == null) end = 0;
   start = start*1000*60;
   end = end*1000*60;
   if (start > 0 && stop > 0 && start > stop) return null;

   return { LABEL : lbl, COND : cnd, START: start, END: end };
}




/********************************************************************************/
/*                                                                              */
/*      Latch sensor                                                            */
/*                                                                              */
/********************************************************************************/

function checkLatchSensorValid()
{
   var fg = getLatchValues() != null;

   if (fg) $("#latchdefine").removeAttr('disabled');
   else $("#latchdefine").attr('disabled','disabled');
}




function addLatchSensor()
{
   var formdata = getLatchValues();
   if (formdata == null) return;

   var url = "/rest/sensor/createlatch";

   $.ajax({ action: "test",
            type: "POST",
            cache: false,
            url: url,
            data: formdata,
            dataType: 'json',
            error: afterError,
            success: afterCreate } );
}




function getLatchValues()
{
   var lbl = $("#latchsensorlabel").val();
   if (lbl == "" || lbl == null) return null;

   var cnd = $("#latchsensorcond").val();
   if (cnd == "" || cnd == "NONE" || cnd == null) return null;

   var reset = $("#latchreset").val();
   var after = $("#latchafter").val();
   if (reset == "" && after == "") return null;
   if (reset == "") reset = null;
   if (after == '' || after == null || after <= 0) after = 0;
   if (reset == null && after == 0) return null;

   after = after*1000*60;
   if (after > 0) reset = null;

   return { LABEL : lbl, COND : cnd, TIME: reset, OFFAFTER: after };
}



/********************************************************************************/
/*                                                                              */
/*      Combination (OR) sensor                                                 */
/*                                                                              */
/********************************************************************************/

function checkComboSensorValid()
{
   var fg = getComboValues() != null;

   if (fg) $("#combodefine").removeAttr('disabled');
   else $("#combodefine").attr('disabled','disabled');

   for (var i = 3; i <= 12; ++i) {
      var val = $("#combocond" + i).val();
      if (val != '') forceshow($("#comborow" + (i+1)));
      else break;
    }
}




function addComboSensor()
{
   var formdata = getComboValues();
   if (formdata == null) return;

   var url = "/rest/sensor/createor";

   $.ajax({ action: "test",
            type: "POST",
            cache: false,
            url: url,
            data: formdata,
            dataType: 'json',
            error: afterError,
            success: afterCreate } );
}




function getComboValues()
{
   var lbl = $("#combosensorlabel").val();
   if (lbl == "" || lbl == null) return null;

   var rslt = { };
   for (var i = 1; i <= 12; ++i) {
      var val = $("#combocond" + i).val();
      if (val != '') rslt[val] = true;
    }
   var xml = "<CONDITIONS>";
   var cnt = 0;
   for (var cnd in rslt) {
      ++cnt;
      xml += "<CONDITION NAME='" + cnd + "' />";
    }
   if (cnt < 2) return null;
   xml += "</CONDITIONS>";

   return { LABEL : lbl, CONDITIONS : xml };
}



/********************************************************************************/
/*                                                                              */
/*      Helper functions                                                        */
/*                                                                              */
/********************************************************************************/

function afterCreate()
{
   window.location.replace("/");
}


function afterError()
{
   alert("Problem creating sensor");
}




/* end of upodsensor.js */
