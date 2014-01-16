// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.
window.onload = function() {
 $("#selectable").selectable();
 $("li:first").addClass('ui-selected');
};

function searchKeyword(){
 var value = $("#inputKeywordSearch").val();
 $.getJSON("ajax/keyword/matches/".concat(value),function(result){
    $("tbody tr").remove();
    $.each(result, function(i){
	$('#tbResult > tbody:last').append('<tr><td>'.concat(result[i]["keyword"])+'</td><td>'.concat(result[i]["type"])+'</td><td>'.concat(result[i]["description"])+'</td></tr>');
    });
  });
}


