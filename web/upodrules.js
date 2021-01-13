/********************************************************************************/
/*                                                                              */
/*              upodrules.js                                                    */
/*                                                                              */
/*      Code for maintaining a list of rules                                    */
/*                                                                              */
/********************************************************************************/




/********************************************************************************/
/*                                                                              */
/*      Global variables                                                        */
/*                                                                              */
/********************************************************************************/

var current_rule = undefined;
var active_rules = undefined;
var active_priorities = undefined;



/********************************************************************************/
/*                                                                              */
/*      Initilizations                                                          */
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
});




/********************************************************************************/
/*                                                                              */
/*      Rule setup                                                              */
/*                                                                              */
/********************************************************************************/

function setupRules(rules)
{
   if (rules == undefined) rules = rule_set;

   active_rules = rules;

   active_priorities = [];

   var html = "";
   for (var i = 0; i < active_rules.length; ++i) {
      var rule = active_rules[i];
      active_priorities.push(rule.getPriority());
      var rid = "rule_" + rule.getId();
      html += "<li class='ui-corner-all' id='" + rid + "'>" +
         "<div class='handle'><span class='ui-icon ui-icon-carat-2-n-s'></span></div>" +
         rule.getDescription() + "</li>";
    }
   // console.log(html);
   $('#rulelist').html(html);
}




/********************************************************************************/
/*                                                                              */
/*      Handle rule selection                                                   */
/*                                                                              */
/********************************************************************************/

function ruleSelected(elt,fg)
{
   if (fg) {
      var rid = getRuleIdFromElement(elt.selected);
      var rule = findRule(rid);
      current_rule = rule;
    }
   else current_rule = undefined;

   if (current_rule == undefined) {
      $('#editrulebtn').attr('disabled','disabled');
      $('#deleterulebtn').attr('disabled','disabled');
    }
   else {
      $('#editrulebtn').removeAttr('disabled');
      $('#deleterulebtn').removeAttr('disabled');
    }
}


function rulesSorted(ui)
{
   updateRulePriorities();
}



function removeRuleDisplay(rid)
{
   var liid = '#rule_' + rid;
   $(liid).remove();
}



function getRuleIdFromElement(elt)
{
   return elt.id.substr(5);
}



function updateRulePriorities()
{
   var rls = $("#rulelist li");
   var len = rls.length;
   if (len == 0) return;
   var delta = 0;
   delta = 60.0/len;            // always set new priorities, don't reuse old

   var nact = [];
   var npri = [];

   $("#rulelist li").each(function(idx,elt) {
                             var val = 80-idx*delta;
                             if (delta == 0) val = active_priorities[idx];
                             var rid = getRuleIdFromElement(elt);
                             var rule = null;
                             for (var i = 0; i < rule_set.length; ++i) {
                                var x = rule_set[i];
                                if (x.getId() == rid) {
                                   rule = x;
                                   break;
                                 }
                              }
                             var opr = -1;
                             if (rule != null) {
                                opr = rule.getPriority();
                                nact.push(rule);
                                npri.push(val);
                                rule.setPriority(val);
                              }
                             if (opr != val) {
                                updateRulePriority(val,rid);
                              }
                           });

   active_rules = nact;
   active_priorities = npri;
}



function updateRulePriority(val,rid)
{
   var cmd = "<CHANGE_RULE_PRIORITY PRIORITY='" + val + "'><RULE ID='" + rid + "'>" +
      "</RULE></CHANGE_RULE_PRIORITY>";
   loadJson(cmd);
}



function deleteRule(rule)
{
   if (rule == undefined) return;

   var rid = rule.getId();

   var cmd = "<REMOVE_RULE><RULE ID='" + rid + "'/></REMOVE_RULE>";
   loadJson(cmd,function(rslt) { removeRule(rslt); });

   removeRuleDisplay(rid);
}




/* end of upodrules.js */

























