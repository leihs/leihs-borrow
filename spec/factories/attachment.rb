class Attachment < Sequel::Model
  many_to_one(:leihs_model, class: :Model, key: :model_id)
  many_to_one(:item)
end

FactoryBot.define do
  factory :attachment do
    transient do 
      real_filename { 'secd.pdf' }
    end

    filename { real_filename }
    content_type { 'application/pdf' }
    size { 160000 }

    after(:build) do |attachment, trans|
      file_path = "spec/files/#{trans.real_filename}"
      md_ext = MetadataExtractor.new(file_path)
      file = File.new(file_path)

      attachment.content = Base64.encode64(file.read)
      attachment.metadata = md_ext.data.to_display_hash.to_json
    end
  end
end
