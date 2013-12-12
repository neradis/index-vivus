class AjaxController < ApplicationController
    include DictionaryHelper

    def initialize
        super
        @keyword_search_service = KeywordSearchService.new
    end

    def get_keyword_matches
        matches = @keyword_search_service.getMatches( params[:keyword] )
        render :json => matches
    end

    def get_keyword_completions
        completions = @keyword_search_service.getCompletions( params[:prefix] )
        render :json => completions
    end

    def get_fulltext_matches
        render :json => []
    end
end
