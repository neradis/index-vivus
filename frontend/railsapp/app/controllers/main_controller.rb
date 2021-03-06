java_import 'de.fusionfactory.index_vivus.models.scalaimpl.DictionaryEntry'
java_import 'de.fusionfactory.index_vivus.services.Language'

class MainController < ApplicationController

	helper_method :get_dictionary_entry, :get_language_enum, :java_lang_to_s

    def index
        render "frontpage"
    end

    def details
        render "entry-details"
    end

    private

    def get_dictionary_entry(id)
    	DictionaryEntry.fetch_by_id(id).get
    end

    def get_language_enum
        Language
    end

    def java_lang_to_s(lang)
        case lang
        when Language::LATIN
            "latin"
        when Language::GREEK
            "greek"
        else
            raise "Bad language given: #{lang}"
        end
    end
end
