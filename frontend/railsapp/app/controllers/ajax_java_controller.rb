java_import 'de.fusionfactory.index_vivus.testing.KeywordSearchServiceMock'
java_import 'de.fusionfactory.index_vivus.services.Language'



class AjaxJavaController < ApplicationController
    include DictionaryHelper

    def initialize
        super
        @@language_by_string = {'latin' => Language::LATIN, 
                                'greek' => Language::GREEK}
        @keyword_search_service = KeywordSearchServiceMock.new
    end

    def get_keyword_matches
        matches = @keyword_search_service.get_matches( params[:keyword], @@language_by_string[params[:lang]] )
        render :text => matches.join(', ')
    end

    def get_keyword_completions
      completions 'unmodified'  
      begin
          completions = @keyword_search_service.get_completions( params[:prefix], @@language_by_string[params[:lang]])
        rescue Exception
          completions = "error"
        end
        render :json => completions
    end

    def get_fulltext_matches
        render :json => []
    end
end
