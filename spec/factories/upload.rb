class Upload < Sequel::Model(:procurement_uploads)
end

FactoryBot.define do
  factory :upload, class: Upload do
    transient do
      real_filename 'secd.pdf'
    end

    filename { real_filename }
    content_type 'application/pdf'
    size 56000
    exiftool_version MetadataExtractor::EXIFTOOL_VERSION
    exiftool_options MetadataExtractor::EXIFTOOL_CMD_LINE_OPTIONS

    after(:build) do |upload, evaluator|
      file_path = "spec/files/#{evaluator.real_filename}"
      md_ext = MetadataExtractor.new(file_path)
      file = File.new(file_path)

      upload.content = Base64.encode64(file.read)
      upload.metadata = md_ext.data.to_display_hash.to_json
    end
  end
end
