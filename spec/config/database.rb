require 'sequel'

require 'logger'

def db_name
  ENV['LEIHS_DATABASE_NAME'] || ENV['DB_NAME'] || 'leihs'
end

def db_port
  Integer(ENV['DB_PORT'].presence || ENV['PGPORT'].presence || 5432)
end

def db_con_str
  logger = Logger.new(STDOUT)
  s = 'postgres://' \
    + (ENV['PGUSER'].presence || 'postgres') \
    + ((pw = (ENV['DB_PASSWORD'].presence || ENV['PGPASSWORD'].presence)) ? ":#{pw}" : "") \
    + '@' + (ENV['PGHOST'].presence || 'localhost') \
    + ':' + (db_port).to_s \
    + '/' + (db_name)
  logger.info "SEQUEL CONN #{s}"
  s
end

def database
  @database ||= Sequel.connect(db_con_str)
end



def with_disabled_trigger(table, trigger)
  t_sql = trigger == :all ? 'ALL' : trigger
  database.run "ALTER TABLE #{table} DISABLE TRIGGER #{t_sql}"
  result = yield
  database.run "ALTER TABLE #{table} ENABLE TRIGGER #{t_sql}"
  result
end

def with_disabled_triggers
  database.run 'SET session_replication_role = replica;'
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

  database[sql].map { |r| r[:table_name] }.reject do |tn|
    %w[schema_migrations translations_default].include?(tn)
  end.join(', ')
    .tap { |tables| database.run " TRUNCATE TABLE #{tables} CASCADE; " }
end

RSpec.configure do |config|
  config.before(:example) do
    clean_db
    system("LEIHS_DATABASE_NAME=#{db_name} ./database/scripts/restore-seeds")
    if sql_file = ENV["SQL_FILE"].presence
      system("cat #{sql_file} | psql --quiet -d #{db_name}")
    else
      system("bin/get-translations | psql --quiet -d #{db_name}")
    end
  end
  # config.after(:suite) do
  #   clean_db
  # end
end

database.extension :pg_json
