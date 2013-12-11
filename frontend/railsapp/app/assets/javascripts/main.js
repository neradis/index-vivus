// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.
window.onload = function() {
 hide();
 $("#selectable").selectable();
 $("li:first").addClass('ui-selected');
};