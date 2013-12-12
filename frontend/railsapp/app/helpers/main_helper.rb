# encoding: UTF-8
module MainHelper
    class DictionaryEntry
        attr_accessor :keyword, :group, :type, :description

        def initialize(keyword, group, type, description)
            @keyword = keyword
            @group = group
            @type = type
            @description = description
        end
    end

    class KeywordSearchService
        DictEntries = [
            DictionaryEntry.new("verbum", 1, "Noun", "das Wort, der Ausdruck, im Plur. die Worte, Ausdrücke, die Rede, im allg."),
            DictionaryEntry.new("verbum", 2, "Noun", "A) die bloße Rede, das leere Wort, der Schein, Cic.: alci dare verba, leere Worte bieten = etwas aufbinden, anführen, hintergehen, überlisten, betrügen, hinters Licht führen, täuschen, ein Schnippchen schlagen
B) Plur. verba, Witze, Späße, quibus sunt verba sine penu ac pecunia, die spaßreich sind, aber arm an Geld u. Proviant, 
C) kollektiv, das Wort, der Ausdruck, a) übh. = der Ausspruch, die Äußerung
D) als gramm. t.t., das Verbum, das Zeitwort, verba temporalia"),
            DictionaryEntry.new("index", 1, "Noun", "der Anzeiger, 
I) eig., v. Menschen,  im allg., der Entdecker, Angeber, insbes., der Angeber, Verräter, Spion
II) übtr., v. lebl. Subjj.: im allg., der Anzeiger, die Anzeigerin, die Anzeige, das Kennzeichen, der Entdecker, Verräter, die Verräterin usw. 
III) Ov.: digitus index, der Zeigefinger"),
            DictionaryEntry.new("index", 2, "Noun", "der Anzeiger, insbes.: 
1) das Register, Verzeichnis, der Katalog (vollst. liber index) u. kurzer Inhalt, Inhaltssumme
2) der Titel, die Aufschrift, einer Schrift
3) der Probierstein"),
            DictionaryEntry.new("requiro", 1, "Verb", "I) hersuchen, zur Stelle suchen, aufsuchen, 
1) eig.: cervam, Gell.: libros, Cic.: alqm, Ter.: columbae evolitant ad requirendos cibos, Colum. – 
2) übtr.: a) vermissen, b) verlangen, erfordern, für nötig halten"),
            DictionaryEntry.new("requiro", 2, "Verb", "II) nach etw. fragen, forschen, nachfragen, nachforschen, sich erkundigen, 
1) im allg., mit u. ohne ab od. ex od. de alqo (bei jmd.), alqd (nach etw.) od. de alqa re (in betreff usw.)– 
2) prägn.: a) untersuchen, b) erforschen, Nachricht einziehen")
        ]

        def getMatches(keyword)
            matches = []
            keyword.downcase!

            DictEntries.each do |entry|
                if entry.keyword.downcase == keyword
                    matches.append(entry)
                end
            end

            return matches
        end

        def getCompletions(keyword)
            completions = []
            keyword.downcase!

            DictEntries.each do |entry|
                if entry.keyword.downcase.starts_with(keyword)
                    completions.append(entry.keyword)
                end
            end

            return completions
        end
    end
end
