class Image < Sequel::Model
  many_to_one(:category, key: :target_id)
  many_to_one(:leihs_model, key: :target_id)
  one_to_many(:thumbnails, class: self, key: :parent_id)
end

FactoryBot.define do
  factory :image do
    transient do 
      real_filename { 'lisp-machine.jpg' }
      thumbnails { [] }
    end

    trait :for_leihs_model do
      target_type { 'Model' }
    end

    trait :for_category do
      target_type { 'ModelGroup' }
    end

    filename { real_filename }
    content_type { 'image/jpeg' }
    size { 160000 }

    after(:build) do |image, trans|
      unless image.target_type
        raise '`target_type` is nil. Use one of the traits!'
      end

      file_path = "spec/files/#{trans.real_filename}"
      md_ext = MetadataExtractor.new(file_path)
      file = File.new(file_path)

      image.content = Base64.encode64(file.read)
      image.metadata = md_ext.data.to_display_hash.to_json
    end

    after(:create) do |image, trans|
      trans.thumbnails.each do |thumbnail|
        image.add_thumbnail(thumbnail)
      end
    end
  end
end
