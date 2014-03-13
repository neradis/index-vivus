(function($) {
    $(function() {
        var $keywordForm  = $('#keywordSearch > form');
        var $keywordInput = $('#inputKeywordSearch');
        var $langSelector = $('#language-selector');

        $langSelector.selectable({
            selected: function() {
                $('#inputKeywordSearch').autocomplete( "search" );
            }
        });

        $langSelector.find("> li:first").addClass('ui-selected');


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


        function getKeywordLanguage() {
            return $langSelector.find('> .ui-selected').data('lang');;
        }

        function searchKeyword(value) {
            var lang = getKeywordLanguage();

            $.getJSON("ajax/keyword/matches/"+encodeURIComponent(lang)+"/"+encodeURIComponent(value), function(result) {
                printSearchResults(result);
            });
        }
    });
})(jQuery);

