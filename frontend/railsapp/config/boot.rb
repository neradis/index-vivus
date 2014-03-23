require 'rubygems'

# Set up gems listed in the Gemfile.
ENV['BUNDLE_GEMFILE'] ||= File.expand_path('../../Gemfile', __FILE__)

require 'java'

require File.expand_path('../../add_classpath.rb', __FILE__)

Java::DeFusionfactoryIndex_vivusTestingFixtures::LoadFixtures.ensureFixturesForDevelopment

require 'bundler/setup' if File.exists?(ENV['BUNDLE_GEMFILE'])

module IndexVivusAdditions
    # a simple wrapper to memorize the startup time of the application (we use this time for :last_modified for
    # requests that involves only data that remains stable during the runtime of the server)
    module Rails
        STARTUP_TIME = Time.now
    end
end
