require 'rubygems'

# Set up gems listed in the Gemfile.
ENV['BUNDLE_GEMFILE'] ||= File.expand_path('../../Gemfile', __FILE__)

require 'java'

require File.expand_path('../../add_classpath.rb', __FILE__)

Java::DeFusionfactoryIndex_vivusTestingFixtures::LoadFixtures.ensureFixturesForDevelopment

require 'bundler/setup' if File.exists?(ENV['BUNDLE_GEMFILE'])
