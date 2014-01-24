java_import 'de.fusionfactory.index_vivus.services.scalaimpl.KeywordSearchService'
java_import 'de.fusionfactory.index_vivus.services.Language'

class AjaxController < ApplicationController

    def initialize
        super
        @@language_by_string = {'latin' => Language::LATIN, 
                                'greek' => Language::GREEK}
        @keyword_search_service = KeywordSearchService::get_instance
    end

    def get_keyword_matches
        matches = @keyword_search_service.get_matches( params[:keyword], @@language_by_string[params[:lang]] )
        render :json => serialize_matches(matches)
    end

    def get_keyword_completions
        begin
            completions = @keyword_search_service.get_completions( params[:prefix], @@language_by_string[params[:lang]])
        rescue Java::JavaUtil::NoSuchElementException => nse
            raise "Unknown language: #{params[:lang]}"
        rescue Exception => e
            completions = ["error #{e} (#{e.class})"]
        end
        render :json => completions
    end

    def get_fulltext_matches
        render :json => []
    end

    private

    def serialize_matches matches
        serialized = []

        matches.each do |match|
            serialized.push ({
                :id         => match.get_id,
                :keyword    => match.get_keyword,
                :type       => match.get_word_type.to_s,
                :description=> match.get_description
            })
        end

        return serialized
    end
end
