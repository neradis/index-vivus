// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.
window.onload = function() {
    $("#language-selector").selectable({
        selected: function() {
            $('#inputKeywordSearch').autocomplete( "search" );
        }
    });

    $("li:first").addClass('ui-selected');
};

function searchKeyword(){
    var value = $("#inputKeywordSearch").val();
    var lang = $("#language-selector .ui-selected").attr("data-lang");
    $.getJSON("ajax/keyword/matches/"+ lang +"/"+(value),function(result){
        $("tbody tr").remove();
        $.each(result, function(i){
	    $('#tbResult > tbody:last').append('<tr><td>' + (result[i]["keyword"])+'</td><td>' + (result[i]["type"])+'</td><td>' + (result[i]["description"])+'</td></tr>');
        });
    });
}

function searchFulltext(){
    var value = $("#inputFulltextSearch").val();
    $.getJSON("ajax/fulltext/matches/"+(value),function(result){
        $("tbody tr").remove();
        $.each(result, function(i){
	    $('#tbResult > tbody:last').append('<tr><td>' + (result[i]["keyword"])+'</td><td>' + (result[i]["type"])+'</td><td>' + (result[i]["description"]) + '</td></tr>');
        });
    });
}



