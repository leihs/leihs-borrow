require 'addressable'
require 'sequel'

DB_ENV = ENV['LEIHS_DATABASE_URL'].presence

def http_uri
  # trick Addressable to parse db urls
  @http_uri ||= \
    Addressable::URI.parse DB_ENV.gsub(/^jdbc:postgresql/,'http').gsub(/^postgres/,'http')
end

def database
  @database ||= \
    begin
      db_url = 'postgres://' \
        + (http_uri.user.presence || ENV['PGUSER'].presence || 'postgres') \
        + ((pw = (http_uri.password.presence || ENV['PGPASSWORD'].presence)) ? ":#{pw}" : "") \
        + '@' + (http_uri.host.presence || ENV['PGHOST'].presence || ENV['PGHOSTADDR'].presence || 'localhost') \
        + ':' + (http_uri.port.presence || ENV['PGPORT'].presence || 5432).to_s \
        + '/' + ( http_uri.path.presence.try(:gsub,/^\//,'') || ENV['PGDATABASE'].presence || 'leihs_test') \
        + '?pool=5'

      Sequel.connect(db_url)
    end
end

def with_disabled_triggers
  database.run 'SET session_replication_role = REPLICA;'
  result = yield
  database.run 'SET session_replication_role = DEFAULT;'
  result
end

def clean_db
  sql = <<-SQL
    SELECT table_name
      FROM information_schema.tables
    WHERE table_type = 'BASE TABLE'
    AND table_schema = 'public'
    ORDER BY table_type, table_name;
  SQL

  database[sql]
    .map { |r| r[:table_name] }
    .reject { |tn| ['schema_migrations', 'translations_default'].include?(tn) }
    .join(', ')
    .tap { |tables| database.run " TRUNCATE TABLE #{tables} CASCADE; " }
end

RSpec.configure do |config|
  config.before(:example)  do
    clean_db
    system("DATABASE_NAME=#{http_uri.basename} ./database/scripts/restore-seeds")
    system("psql -d #{http_uri.basename} < ./bin/translations.sql > /dev/null")
  end
  # config.after(:suite) do
  #   clean_db
  # end
end

database.extension :pg_json
