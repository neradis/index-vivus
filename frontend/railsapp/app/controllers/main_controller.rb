class MainController < ApplicationController
	include DictionaryHelper
	helper_method :get_dictionary_entry

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
end
