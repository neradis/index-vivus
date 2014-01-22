(function($) {
    $(function() {
        var $keywordInput = $('#inputKeywordSearch');
        var $langSelector = $('#language-selector');

        $keywordInput.autocomplete({
            source : function(request, responseCallback) {
                var term = request.term;
                if (term.length < 2) {
                    return;
                }

                var lang = $langSelector.find('> .ui-selected').data('lang');

                $.get('/ajax/keyword/completions/'+encodeURIComponent(lang)+'/'+encodeURIComponent(term), 'json')
                .done(function(data) {
                    responseCallback(data);
                });
            }
        });

        $keywordInput.on('focus', function(event) {
            $(this).autocomplete("search", this.value);
        });
    });
})(jQuery);