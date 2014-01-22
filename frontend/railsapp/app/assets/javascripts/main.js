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

//Widget
window.onload = function() {
 hideWidget();  
 $("#selectable").selectable();
 $("li:first").addClass('ui-selected');
 
 $("#inpudKeywordSeach").blur(function() {
     hideWidget();
 });
 $("#inputKeywordSearch").focus(function() {
     showWidget();
 });
 $("#inputKeywordSearch").keyup(function() {
     parseInput($(this).val());
 });
};

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

function parseInput(value){
	$("#widget input[type=button]").remove();
	var letter = value.substr(value.length-1);
	if(alphabet[letter]==undefined){
		hideWidget();
	}else{
		showWidget();
	}
	for(var i in alphabet[letter]){
		$("#widget").append('<input type="button" class="btn" value="' + alphabet[letter][i] +'">');
		$("#widget input[type=button]").click(function() {
			var value = $(this).attr("value");
			insertLetter(value);
		});
	}
};

function insertLetter(letter){
	var input = $("#inputKeywordSearch").val();
	$("#inputKeywordSearch").val(input.substr(0,input.length-1).concat(letter));
	$("#inputKeywordSearch").focus();
}


function showWidget(){
	value = $("#inputKeywordSearch").val();
	var letter = value.substr(value.length-1);
	if(alphabet[letter]!=undefined && ($("#language-selector .ui-selected").attr("data-lang")=="greek")){
		$("#widget").fadeIn();
	}
	
	$("#widget").hover(function() {
		$("#inputKeywordSearch").unbind("blur");
	}, function(){
		$("#inputKeywordSearch").blur(function() {
			hideWidget();
		});
	});
}

function hideWidget(){
	$("#widget").fadeOut();	
}
