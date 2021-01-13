/********************************************************************************/
/*                                                                              */
/*              smartsigna.js                                                   */
/*                                                                              */
/*      Code to manipulate A-prototype smartsign web page                       */
/*                                                                              */
/********************************************************************************/



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

   // Set up Rule
   setupRules(rule_set);

   // Set up Basic Condition Types
   setupConditions(world);

   // Set up Basic Action Types
   setupActions(world);
}





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




/********************************************************************************/
/*                                                                              */
/*      Main action buttons                                                     */
/*                                                                              */
/********************************************************************************/

function createNewRule()
{
   $("#rulelabel").val("");
   $("#actionlabel").val("");
   $("#rulesubmit").show();
   $("#ruleedit").hide();

   forceshow($('#newrulearea'));
   setRuleType($('#ruletype_1')[0]);
   setActionType($('#actionedittype')[0]);

   performValidityCheck();
}


function finishNewRule()
{
   $('#newrulearea').hide();
}



function deleteSelectedRule()
{
   deleteRule(current_rule);

   $("#deleterulebtn").attr('disabled','disabled');
   $("#editrulebtn").attr('disabled','disabled');
}



function editSelectedRule()
{
   if (current_rule == undefined) return;

   var r1 = current_rule;

   $("#rulesubmit").hide();
   $("#ruleedit").show();
   $("#ruleeditid").val(r1.getId());

   clearRuleArea();

   var xx = $("#ruleeditid");
   var x = xx.val();

   var rid = current_rule.getId();

   $("#rulelabel").val(current_rule.getDescription());

   forceshow($('#newrulearea'));
   var conds = current_rule.getConditions();
   for (var i = 1; i < conds.length; ++i) {
       expandRule($('.andarea')[0]);
     }
   for (var i = 0; i < conds.length; ++i) {
      initCondition(i+1,true,false,conds[i]);
    }

   $('.ruleselect').each(function(idx,elt) { setRuleType(elt); });

   initAction(current_rule.getAction());
}




/* end of smartsigna.js */
