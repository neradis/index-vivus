(function($) {
    $(function() {
        var $keywordInput = $('#inputKeywordSearch');

        $keywordInput.autocomplete({
            source : function(request, responseCallback) {
                var term = request.term;
                if (term.length < 2) {
                    return;
                }

                $.get('/ajax/keyword/completions/'+encodeURIComponent(term), 'json')
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