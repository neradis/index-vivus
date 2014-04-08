(function($) {
    var $keywordForm, $keywordInput, $langSelector;

    $(function() {
        $keywordForm  = $('#keywordSearch > form');
        $keywordInput = $('#inputKeywordSearch');
        $langSelector = $('#language-selector');

        $keywordInput.autocomplete({
            source : function(request, responseCallback) {
                var lang = getKeywordLanguage();
                var term = request.term;
                if (term.length < 2) {
                    return;
                }

                $.get('/ajax/keyword/completions/'+encodeURIComponent(lang)+'/'+encodeURIComponent(term), 'json')
                .done(function(data) {
                    responseCallback(data);
                });
            },

            select : function(event, ui) {
                $keywordInput.val( ui.item.value );
                $keywordForm.submit();
            }
        });

        $keywordInput.on('focus', function(event) {
            $(this).autocomplete("search", this.value);
        });


        $keywordForm.submit(function(event) {
            event.preventDefault();

            if (! $keywordInput.val()) {
                alert("Bitte geben Sie einen Suchbegriff ein.");
                return;
            }

            searchKeyword( $keywordInput.val() );
        });
    });


    function getKeywordLanguage() {
        return $langSelector.is(':checked') ? $langSelector.data('on-value') : $langSelector.data('off-value');
    }

    function searchKeyword(value) {
        var lang = getKeywordLanguage();

        showSearchResultsThrobber();

        $.getJSON("ajax/keyword/matches/"+encodeURIComponent(lang)+"/"+encodeURIComponent(value), function(results) {
            if (results.length == 1) {
                window.location.href = "/details/"+results[0].id;
            } else {
                printSearchResults(results);
            }
        });
    }

    window.getKeywordLanguage = getKeywordLanguage;
})(jQuery);

