require 'edn'
require 'faraday'
require 'rack'

class GraphqlQuery
  attr_reader :response

  def initialize(query, user_id, variables)
    @query = query
    @variables = variables
    @user_id = user_id
    @cookies = get_cookies(user_id)
  end

  def perform
    @response = Faraday.post("#{Constants::LEIHS_BORROW_HTTP_BASE_URL}/borrow/graphql") do |req|
      req.headers['Accept'] = 'application/json'
      req.headers['Content-Type'] = 'application/json'
      req.headers['X-CSRF-Token'] = @cookies['leihs-anti-csrf-token']
      req.body = { query: @query, variables: @variables }.to_json

      cookies = { "leihs-anti-csrf-token" => @cookies['leihs-anti-csrf-token'] }

      if @cookies['leihs-user-session']
        cookies.merge!("leihs-user-session" => @cookies['leihs-user-session'])
      end

      req.headers['Cookie'] = cookies.map { |k, v| "#{k}=#{v}" }.join('; ')
    end

    self
  end

  def result
    JSON.parse @response.body
  end

  def get_cookies(user_id)
    resp = if user = User.find(id: user_id)
             Faraday.post("#{Constants::LEIHS_BORROW_HTTP_BASE_URL}/sign-in",
                          { user: user.email, password: 'password' })
           else
             Faraday.post("#{Constants::LEIHS_BORROW_HTTP_BASE_URL}")
           end

    Rack::Utils.parse_cookies_header(resp.headers['set-cookie'])
  end
end

RSpec.shared_context 'graphql client' do
  def query(q, user_id = nil, variables = {})
    GraphqlQuery.new(q, user_id, variables).perform.result.deep_symbolize_keys
  end

  def expect_graphql_result(result, compared)
    expect(result[:errors]).to be_nil
    expect(result[:data]).to eq(compared)
  end

  def expect_graphql_error(result)
    expect(result[:data]).to be_nil
    expect(result[:errors]).not_to be_empty
    yield if block_given?
  end
end

RSpec.configure do |config|
  config.include_context 'graphql client'
end
