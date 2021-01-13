/********************************************************************************/
/*                                                                              */
/*              smartsignb.js                                                   */
/*                                                                              */
/*      Code to manipulate B-prototype smartsign web page                       */
/*                                                                              */
/********************************************************************************/



/********************************************************************************/
/*                                                                              */
/*      Global variables                                                        */
/*                                                                              */
/********************************************************************************/

var     cur_condition = null;




/********************************************************************************/
/*                                                                              */
/*      Setup on load                                                           */
/*                                                                              */
/********************************************************************************/

$(function() {
        $("#rulelist").sortable({ handle: ".handle",
                                  update: function(event,ui) { rulesSorted(ui); }
                                 });
        $("#rulelist").selectable({
                          selected : function(event,ui) { ruleSelected(ui,true); },
                          unselected : function(event,ui) { ruleSelected(ui,false); }
                        });
        $('#editrulebtn').attr('disabled','disabled');
        $('#deleterulebtn').attr('disabled','disabled');
        loadDescription(setupPage);
});



/********************************************************************************/
/*                                                                              */
/*      Handle setup after world is loaded                                      */
/*                                                                              */
/********************************************************************************/

function setupPage(world)
{
   setupCommon(world);

   // Set up Basic Condition Types
   setupConditions(world);

   // Set up Basic Action Types
   setupActions(world);

   // show all rules initially
   setupRules(rule_set);

   setRuleType($('#ruletype_1')[0]);
   setActionType($('#actionedittype')[0]);
}



/********************************************************************************/
/*                                                                              */
/*      Conflict detection                                                      */
/*                                                                              */
/********************************************************************************/

function checkConflicts()
{
   if (!validateConditions(null,true)) return;

   var obj = getRuleValues(false);
   var xml = addRuleCondition(obj);

   if (xml == cur_condition) return;

   var cmd = "<LIST_RESTRICT>" + xml + "</LIST_RESTRICT>";
   loadJson(cmd,function(jresp) {
               var conflicts = buildRuleSet(jresp);
               setupRules(conflicts);
             });
}




/********************************************************************************/
/*                                                                              */
/*      Rule setup                                                              */
/*                                                                              */
/********************************************************************************/

function getNewPriority()
{
   var next = -1;
   for (var i = 0; i < rule_set.length; ++i) {
      var p = rule_set[i].getPriority();
      if (current_rule == null) {
         return p/2.0;
       }
      else if (current_rule == rule_set[i]) {
         next = p;
       }
      else if (next >= 0) {
         return (p-next)/2.0;
       }
    }
   if (next >= 0) return (100-next)/2.0;

   return 50;
}




function finishNewRule()
{
   clearRuleArea();
}






/* end of smartsignb.js */
