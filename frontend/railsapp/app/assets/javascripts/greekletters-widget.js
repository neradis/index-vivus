(function($) {
    var $keywordInput, $widget;

    $(function() {
        $keywordInput = $("#inputKeywordSearch");
        $widget = $("#widget");

        hideWidget(true);

        $keywordInput.blur(function() {
            hideWidget();
        });
        $keywordInput.focus(function() {
            parseInput($(this).val());
        });
        $keywordInput.keyup(function() {
            parseInput($(this).val());
        });
        $("#language-selector").change(function() {
            hideWidget();
        });
    });

    var alphabet = new Array;
    alphabet["a"] = new Array("ά","ὰ","ᾶ","ἀ","ἄ","ἂ","ἆ","ἁ","ἅ","ἃ","ἇ","ᾱ","ᾰ");
    alphabet["A"] = new Array("Ά","Ὰ","Ἀ","Ἄ","Ἂ","Ἆ","Ἁ","Ἅ","Ἃ","Ἇ","Ᾱ","Ᾰ");
    alphabet["e"] = new Array("έ","ὲ","ἐ","ἔ","ἒ","ἑ","ἕ","ἓ");
    alphabet["E"] = new Array("Έ","Ὲ","Ἐ","Ἔ","Ἒ","Ἑ","Ἕ","Ἓ");
    alphabet["h"] = new Array("ή","ὴ","ῆ","ἠ","ἤ","ἢ","ἦ","ἡ","ἥ","ἣ","ἧ");
    alphabet["H"] = new Array("Ή","Ὴ","Ἠ","Ἤ","Ἢ","Ἦ","Ἡ","Ἥ","Ἣ","Ἧ");
    alphabet["I"] = new Array("Ί","Ὶ","Ἰ","Ἴ","Ἲ","Ἶ","Ἱ","Ἵ","Ἳ","Ἷ","Ϊ","Ῑ","Ῐ");
    alphabet["i"] = new Array("ί","ὶ","ῖ","ἰ","ἴ","ἲ","ἶ","ἱ","ἵ","ἳ","ἷ","ϊ","ΐ","ῒ","ῗ","ῑ","ῐ");
    alphabet["O"] = new Array("Ό","Ὸ","Ὀ","Ὄ","Ὂ","Ὁ","Ὅ","Ὃ");
    alphabet["U"] = new Array("Ύ","Ὺ","Ὑ","Ὕ","Ὓ","Ὗ","Ϋ","Ῡ","Ῠ");
    alphabet["W"] = new Array("Ώ","Ὼ","Ὠ","Ὤ","Ὢ","Ὦ","Ὡ","Ὥ","Ὣ","Ὧ");
    alphabet["o"] = new Array("ό","ὸ","ὀ","ὄ","ὂ","ὁ","ὅ","ὃ");
    alphabet["u"] = new Array("ύ","ὺ","ῦ","ὐ","ὔ","ὒ","ὖ","ὑ","ὕ","ὓ","ὗ","ϋ","ΰ","ῢ","ῧ","ῡ","ῠ");
    alphabet["w"] = new Array("ώ","ὼ","ῶ","ὠ","ὤ","ὢ","ὦ","ὡ","ὥ","ὣ","ὧ");

    function parseInput(value) {
        var letter = value.substr(value.length-1);
        if(alphabet[letter] == undefined) {
            hideWidget();
            return;
        } else {
            $widget.find("input[type=button]").remove();
            showWidget();
        }

        var diacritics = alphabet[letter];
        for(var i=0; i<diacritics.length; i++) {
            $widget.append(
                $('<input type="button" class="btn" />')
                .val(diacritics[i])
                .click(function() {
                    insertLetter( $(this).val() );
                })
            );
        }
    };

    function insertLetter(letter) {
        var input = $keywordInput.val();

        $keywordInput.val( input.substr(0,input.length-1).concat(letter) );
        $keywordInput.focus();
    }


    function showWidget() {
        value = $keywordInput.val();
        var letter = value.substr(value.length-1);

        if(alphabet[letter] != undefined && getKeywordLanguage() == "greek") {
            $widget.fadeIn();
        }
        
        $widget.hover(function() {
            $keywordInput.unbind("blur");
        }, function() {
            $keywordInput.blur(function() {
                hideWidget();
            });
        });
    }

    function hideWidget(immediately) {
        if (immediately) {
            $widget.fadeOut(0);
        } else {
            $widget.fadeOut();
        }
    }
})(jQuery);