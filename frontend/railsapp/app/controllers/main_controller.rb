java_import 'de.fusionfactory.index_vivus.services.Language'

class MainController < ApplicationController
	include DictionaryHelper
	helper_method :get_dictionary_entry, :get_language_enum

    def index
        render "frontpage"
    end

    def details
        render "entry-details"
    end

    private

    def get_dictionary_entry(id)
    	DictionaryEntry.by_id(id)
    end

    def get_language_enum
        Language
    end
end
