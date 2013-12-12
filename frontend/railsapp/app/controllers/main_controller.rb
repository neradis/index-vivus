class MainController < ApplicationController
    include MainHelper

    helper_method :get_keyword_matches, :get_fulltext_matches

    def initialize
        super
        @keyword_search_service = KeywordSearchService.new
    end

    def index
        render "frontpage"
    end

    def details
        render "entry-details"
    end

    def get_keyword_matches(keyword)
        @keyword_search_service.getMatches(keyword)
    end

    def get_fulltext_matches(query)
        []
    end
end
