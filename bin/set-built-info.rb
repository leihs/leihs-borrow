#!/usr/bin/env ruby

# NOTE: if git info already given via env vars, dont use git at all!
commit_id = ENV['BUILD_COMMIT_ID']
tree_id = ENV['BUILD_TREE_ID']

require 'bundler/inline'
gemfile do
  source 'https://rubygems.org'
  gem 'git', '= 2.1.1', require: false
  gem 'pry', '= 0.14.1'
  gem 'activesupport', '= 5.2.4.5'
end

require 'active_support/all'
require 'date'
require 'pathname'
require 'pry'
require 'socket'
require 'yaml'

__file__ = __FILE__
script = Pathname(__file__)

PROJECT_DIR = Pathname(File.expand_path(File.dirname(__FILE__))).join('..')

unless commit_id.present? && tree_id.present?
  require 'git'
  project = Git.open(PROJECT_DIR)
  commit_id = project.log[0].sha
  head = project.object('HEAD')
  tree_id = head.gtree.objectish
end

IO.write(PROJECT_DIR.join("resources").join("built-info.yml"),
         {commit_id: commit_id,
          hostname: Socket.gethostname,
          os: Gem::Platform.local.to_s,
          timestamp: DateTime.now.iso8601,
          tree_id: tree_id, }.with_indifferent_access.to_h.to_yaml)
