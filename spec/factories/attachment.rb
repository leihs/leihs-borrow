class Attachment < Sequel::Model(:procurement_attachments)
end

FactoryBot.define do
  f_name = 'secd.pdf'
  f_path = "spec/files/#{f_name}"
  me = MetadataExtractor.new(f_path)

  factory :attachment, class: Attachment do
    filename f_name
    content_type 'application/pdf'
    size 56000
    content Base64.encode64(File.new(f_path).read)
    metadata me.data.to_display_hash.to_json
    request_id { create(:request).id }
    exiftool_version MetadataExtractor::EXIFTOOL_VERSION
    exiftool_options MetadataExtractor::EXIFTOOL_CMD_LINE_OPTIONS
  end
end
