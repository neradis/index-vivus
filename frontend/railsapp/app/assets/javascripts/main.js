// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.
(function($) {
    var wordTypeLabels = {
        "ADJECTIVE"     : "Adjektiv",
        "ADVERB"        : "Adverb",
        "CONJUNCTION"   : "Konjunktion",
        "INTERJECTION"  : "Interjektion",
        "NOUN"          : "Nomen",
        "OTHER"         : "Anderes",
        "PREPOSITION"   : "Präposition",
        "PRONOUN"       : "Pronomen",
        "UNKNOWN"       : "Unbekannt",
        "VERB"          : "Verb"
    };

    function searchFulltext() {
        var value = $("#inputFulltextSearch").val();

        $.getJSON("ajax/fulltext/matches/"+(value), function(result) {
            printSearchResults(result);
        });
    }

    function printSearchResults(matches) {
        var $tr;

        $("#tbResult tbody tr").remove();

        $.each(matches, function(i, match) {
            var detailsUrl = '/details/'+match.id;

            $('#tbResult > tbody:last')
            .append(
                $tr = $('<tr></tr>')
                .append(
                    $('<td></td>').append(
                        $('<a></a>').text(match.keyword).attr('href', detailsUrl)
                    ),
                    $('<td></td>').text( wordTypeLabels[match.type] ),
                    $('<td></td>').text(match.description)
                )
            );

            $tr.find('> td').click(function(event) {
                document.location.href = detailsUrl;
            });
        });
    }


    // Exports:

    window.searchFulltext = searchFulltext;
    window.printSearchResults = printSearchResults;
})(jQuery);