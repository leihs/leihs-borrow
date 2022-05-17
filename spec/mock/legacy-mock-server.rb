require 'active_support/all'
require 'json'
require 'pry'
require 'sinatra'

set :show_exceptions, false

get '/status' do
  'OK'
end

get '/borrow/booking_calendar_availability' do
  case params[:model_id]
  when '91f2c252-ebb4-4265-8806-4669c8626913'
    { list: [
      { d: '2019-10-24',
        visits_count: 0,
        quantity: 1 }
      ]
    }
  when '948ee4ef-b576-4256-996f-38f25030f151'
    { list: [
      { d: Date.yesterday.to_s,
        visits_count: 0,
        quantity: 1 },
      { d: Date.today.to_s,
        visits_count: 0,
        quantity: 1 },
      { d: (Date.today + 1.day).to_s,
        visits_count: 0,
        quantity: 1 }
      ]
    }
  when 'a95259db-b3bc-4324-907f-c4a5811cf049'
    { list: [
      { d: Date.today,
        visits_count: 0,
        quantity: 1 },
      { d: (Date.today + 1.day).to_s,
        visits_count: 0,
        quantity: 1 },
      { d: (Date.today + 2.days).to_s,
        visits_count: 0,
        quantity: 1 }
      ]
    }
  when '8afe4e63-fded-4726-8808-6a097452374e'
    { list: [
      { d: Date.today,
        visits_count: 0,
        quantity: 1 },
      { d: (Date.tomorrow).to_s,
        visits_count: 0,
        quantity: 1 },
      ]
    }
  when 'da28cf22-db3e-4b9d-bfa8-199923b629cf'
    { list: [
      { d: Date.today,
        quantity: 1,
        visits_count: 1 }
      ]
    }
  when '2bc1deb5-9428-4178-afd0-c06bb8d31ff3', '210a4116-162f-4947-bcb0-2d7d1a5c7b1c'
    { list: [
      { d: Date.yesterday.to_s,
        visits_count: 0,
        quantity: 1 },
      { d: Date.today.to_s,
        visits_count: 0,
        quantity: 1 },
      { d: (Date.today + 1.day).to_s,
        visits_count: 0,
        quantity: 1 }
      ]
    }
  else
    raise("Unknown model ID: #{params[:model_id]}")
  end.to_json
end

post '/mail/received' do
  status 202
end
