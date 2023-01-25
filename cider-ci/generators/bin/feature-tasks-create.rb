#!/usr/bin/env ruby

require 'pry'
require 'active_support/all'
require 'json'

PROJECT_DIR = Pathname.new(__FILE__).expand_path.join("../../../..")
features_json = IO.read PROJECT_DIR.join('tmp/features.json')
features = JSON.parse(features_json).with_indifferent_access 

task_template = IO.read(
  PROJECT_DIR.join("cider-ci", "generators", "feature-task-template.yml"))

# name must be unique but they are not: append index
$name_id = {}
def set_name(example)
  name = example[:full_description].remove(example[:description]).strip()
  $name_id[name] = 1 + ($name_id[name] || 0)
  example[:name] = name + " " + $name_id[name].to_s
  example
end

File.open(PROJECT_DIR.join("cider-ci", "generators", "feature-tasks.yml"), "w") do |f|
  features[:examples].map{|example|
    set_name(example)
  }.each{  |example|
    f.write (task_template % example.as_json.transform_keys(&:to_sym))
  }
end
