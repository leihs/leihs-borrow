require 'active_support/all'
require 'json'
require 'pry'
require 'sinatra'

get '/status' do
  'OK'
end

get '/borrow/models/availability' do
  if params[:model_ids].to_set ==
      Set['0cad263d-14b9-4595-9878-7adde7f4f586',
          '1adfe883-3546-4b5c-9ed6-b18f01f77723',
          '29c1bdf9-7764-4e1e-bf9e-902f908be8d5']
    [{ model_id: '0cad263d-14b9-4595-9878-7adde7f4f586',
       inventory_pool_id: '8f613f14-3b6d-4d5c-9804-913e2da1109e',
       quantity: 0 },
     { model_id: '1adfe883-3546-4b5c-9ed6-b18f01f77723',
       inventory_pool_id: '8f613f14-3b6d-4d5c-9804-913e2da1109e',
       quantity: 1 },
     { model_id: '1adfe883-3546-4b5c-9ed6-b18f01f77723',
       inventory_pool_id: '6ce92dd1-cf47-4942-97a1-6bc5b495b425',
       quantity: 1 },
     { model_id: '29c1bdf9-7764-4e1e-bf9e-902f908be8d5',
       inventory_pool_id: '8f613f14-3b6d-4d5c-9804-913e2da1109e',
       quantity: -2 },
     { model_id: '29c1bdf9-7764-4e1e-bf9e-902f908be8d5',
       inventory_pool_id: '6ce92dd1-cf47-4942-97a1-6bc5b495b425',
       quantity: 1 }]
  else
    raise
  end.to_json
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
  when '2bc1deb5-9428-4178-afd0-c06bb8d31ff3', '210a4116-162f-4947-bcb0-2d7d1a5c7b1c'
    { list: [
      { d: '2019-10-24',
        quantity: 1 },
      { d: '2019-10-25',
        quantity: 1 }
      ]
    }
  else
    raise "Unknown model ID: #{params[:model_id]}"
  end.to_json
end
