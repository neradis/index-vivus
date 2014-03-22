// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.
(function($) {
    var $pagination;

    var DESCRIPTION_MAX_LENGTH = 300;
    var FULLTEXT_RESULTS_PER_PAGE = 20;
    var LANGUAGES = ["greek", "latin"];

    $(function() {
        var $fulltextSearchInput = $("#inputFulltextSearch");
        var $fulltextSearchForm = $("#fulltextSearch").parent("form");

        $('#language-selector').bootstrapSwitch();

        $pagination = $('#result > .pagination');

        $fulltextSearchForm.submit(function(event) {
            event.preventDefault();

            if (! $fulltextSearchInput.val()) {
                alert("Bitte geben Sie einen Suchbegriff ein.");
                return;
            }

            searchFulltext( $fulltextSearchInput.val(), 1 );
        });

        loadAllAbbreviations();
    });


    function searchFulltext(value, pageNo) {
        var requestUri = "ajax/fulltext/matches/" + 
                         encodeURIComponent(value) + '/' +
                         encodeURIComponent(FULLTEXT_RESULTS_PER_PAGE) + '/' + 
                         encodeURIComponent(pageNo);

        $.getJSON(requestUri, function(result) {
            var matches = result.hits;

            printSearchResults(matches);
            $pagination.empty();

            doPagination(pageNo, Math.ceil(result.total/FULLTEXT_RESULTS_PER_PAGE), result.hasPrev, result.hasNext, function (switchToPage) {
                searchFulltext(value, switchToPage);
            });
        });
    }

    function printSearchResults(matches) {
        var $tr;

        $('#result').addClass('active');

        $("#tbResult tbody tr").remove();
        $("#tbResult").addClass('active');

        if (matches.length == 0) {
            $pagination.hide();

            $('#tbResult > tbody')
            .addClass('no-results')
            .removeClass('results')
            .append(
                $('<tr></tr>').append(
                    $('<td colspan="2"></td>').text("Keine Ergebnisse")
                )
            );
            return;
        }

        $pagination.show();
        $.each(matches, function(i, match) {
            var detailsUrl = '/details/'+match.id;
            var description = match.description.length < DESCRIPTION_MAX_LENGTH
                            ? match.description
                            : match.description.substr(0, DESCRIPTION_MAX_LENGTH) + "...";

            $('#tbResult > tbody')
            .addClass('results')
            .removeClass('no-results')
            .append(
                $tr = $('<tr></tr>')
                .append(
                    $('<td></td>').append(
                        $('<a></a>').text(match.keyword).attr('href', detailsUrl)
                    ),
                    $('<td></td>').text(description)
                )
            );

            $tr.find('> td').click(function(event) {
                document.location.href = detailsUrl;
            });
        });
    }

    /**
     * @param {Integer} currentPage (page numbers are 1-based)
     * @param {Function} switchPageCallback
     *      function(switchToPage)
     *      @param {Integer} switchToPage   New page no.
     */
    function doPagination(currentPage, totalPages, hasPrev, hasNext, switchPageCallback) {
        $pagination
        .removeClass('active')
        .empty();

        if (hasPrev) {
            $pagination
            .addClass('active')
            .append(
                $('<a class="prev-page"></a>').text("< Seite "+(currentPage-1))
                .click(function() {
                    switchPageCallback(currentPage-1);
                })
            );
        }

        if (hasNext) {
            $pagination
            .addClass('active')
            .append(
                $('<a class="next-page"></a>').text("Seite "+(currentPage+1)+" >")
                .click(function() {
                    switchPageCallback(currentPage+1);
                })
            );
        }

        $pagination.append(
            $('<div class="info"></div>').text("Seite "+currentPage+" von "+totalPages)
        );
    }

    function loadAllAbbreviations() {
        var languages = LANGUAGES;
        var completelyLoaded = 0;

        window.abbreviations = {};
        window.abbreviationsLoaded = false;
        window.abbreviationsLoadedCallbacks = [];   // are called when abbreviations are ready

        for (var i=0; i<languages.length; i++) {
            var lang = languages[i];
            loadAbbreviations(lang, function() {
                completelyLoaded++;

                if (completelyLoaded == languages.length) {
                    allAbbreviationsLoaded();
                }
            });
        }
    }

    function loadAbbreviations(lang, callback) {
        $.get('/ajax/abbreviations/expansions/'+lang, function(json) {
            window.abbreviations[lang] = json;
            callback();
        }, 'json');
    }

    function allAbbreviationsLoaded() {
        window.abbreviationsLoaded = true;

        for (var i=0; i<abbreviationsLoadedCallbacks.length; i++) {
            var callback = abbreviationsLoadedCallbacks[i];
            callback();
        }
    }

    function augmentAbbreviations(domElement, lang) {
        var $text = $(domElement);

        if (!abbreviationsLoaded) {
            abbreviationsLoadedCallbacks.push(function() {
                augmentAbbreviations(domElement, lang);
            });
            return;
        }

        $text.find('abbr').each(function(index, abbrTag) {
            var $abbr = $(abbrTag);
            $abbr.attr('title', abbreviations[lang][$abbr.text()]);
        });
    }


    // Exports:

    window.searchFulltext = searchFulltext;
    window.printSearchResults = printSearchResults;
    window.augmentAbbreviations = augmentAbbreviations;

})(jQuery);

