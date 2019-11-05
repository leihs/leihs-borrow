require 'active_support/all'
require 'json'
require 'pry'
require 'sinatra'

get '/status' do
  'OK'
end

get '/borrow/booking_calendar_availability' do
  case params[:model_id]
  when '91f2c252-ebb4-4265-8806-4669c8626913'
    { list: [
      { d: '2019-10-24',
        quantity: 1 }
      ]
    }
  when '906ac7a7-1f1e-4367-b1f0-fa63052fbd0f'
    dates = (Date.tomorrow..Date.tomorrow + 1.day).map do |d|
      { d: d.strftime, quantity: 2 }
    end
    { list: dates }
  else
    raise 'Unknown model ID'
  end.to_json
end
