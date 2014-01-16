class MainController < ApplicationController
    def index
        render "frontpage"
    end

    def details
        render "entry-details"
    end
end
