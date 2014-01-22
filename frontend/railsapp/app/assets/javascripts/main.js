// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.


function searchFulltext() {
    var value = $("#inputFulltextSearch").val();

    $.getJSON("ajax/fulltext/matches/"+(value), function(result) {
        printSearchResults(result);
    });
}

function printSearchResults(matches) {
    $("tbody tr").remove();

    $.each(matches, function(i, match) {
        $('#tbResult > tbody:last')
        .append(
            $('<tr></tr>').append(
                $('<td></td>').text(match["keyword"]),
                $('<td></td>').text(match["type"]),
                $('<td></td>').text(match["description"])
            )
        )
    });
}
