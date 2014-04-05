java_import 'de.fusionfactory.index_vivus.services.scalaimpl.KeywordSearchService'
java_import 'de.fusionfactory.index_vivus.services.scalaimpl.FullTextSearchService'
java_import 'com.google.common.base.Optional'
java_import 'de.fusionfactory.index_vivus.services.Language'
java_import 'de.fusionfactory.index_vivus.services.scalaimpl.AbbreviationSetsService'

class AjaxController < ApplicationController

    def initialize
        super
        @@language_by_string = {'latin' => Language::LATIN,
                                'greek' => Language::GREEK,
                                'all' => Language::ALL}
        @keyword_search_service = KeywordSearchService::get_instance
        @abbreviation_set_service = AbbreviationSetsService::get_instance
        @fulltext_search_service=FullTextSearchService.new
    end

    def get_keyword_matches
        if stale? :last_modified => IndexVivusAdditions::Rails::STARTUP_TIME
            matches = @keyword_search_service.get_matches(params[:keyword], @@language_by_string[params[:lang]])
            render :json => serialize_matches(matches)
        end
    end

    def get_matches_with_alternative
        if stale? :last_modified => IndexVivusAdditions::Rails::STARTUP_TIME
            alternative_option = params[:alternative].blank? && Optional.absent || Optional.of(params[:alternative])
            matches = @keyword_search_service.get_matches_with_alternative( params[:keyword], alternative_option,
                                                                            @@language_by_string[params[:lang]])
            render :json => serialize_matches(matches)
        end                                                                
    end

    def get_keyword_completions
        if stale? :last_modified => IndexVivusAdditions::Rails::STARTUP_TIME
            begin
                completions = @keyword_search_service.get_completions(params[:prefix], @@language_by_string[params[:lang]])
            rescue Java::JavaUtil::NoSuchElementException => nse
                raise "Unknown language: #{params[:lang]}"
            rescue Exception => e
                completions = ["error #{e} (#{e.class})"]
            end
            render :json => completions
        end
    end

    def get_fulltext_matches
        if stale? :last_modified => IndexVivusAdditions::Rails::STARTUP_TIME
            page = (params[:page] || 1).to_i
            limit = (params[:limit] || 10).to_i

            resultpage = @fulltext_search_service.get_matches(params[:query].to_java, page, limit)

            render :json => {
                :page_no => resultpage.page,
                :total => resultpage.total_hits,
                :hits => serialize_matches(resultpage.hits),
                :hasPrev => resultpage.has_previous_page,
                :hasNext => resultpage.has_next_page,
                :stats_text => resultpage.stat_string
            }
        end
    end

    def get_abbreviation_expansions
        if stale? :last_modified => IndexVivusAdditions::Rails::STARTUP_TIME
            begin
                expansions_map = @abbreviation_set_service.get_abbreviation_expansions(@@language_by_string[params[:lang]])
                expansions = java_hashmap_to_ruby_hash(expansions_map)
            rescue Java::JavaUtil::NoSuchElementException => nse
                raise "Unknown language: #{params[:lang]}"
            rescue Exception => e
                expansions = {:error => "error #{e} (#{e.class})"}
            end

            render :json => expansions
        end
    end


    private

    def serialize_matches matches
        serialized = []

        matches.each do |match|
            serialized.push ({
                :id          => match.get_id,
                :keyword     => match.get_keyword,
                :type        => match.get_word_type.to_s,
                :description => match.get_description
            })
        end

        return serialized
    end

    def java_hashmap_to_ruby_hash (map)
        hash = {}
        map.each { |key, value| hash[key] = value }
        return hash
    end
end
