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
  elsif params[:model_ids] == ['906ac7a7-1f1e-4367-b1f0-fa63052fbd0f']
    [{ model_id: '906ac7a7-1f1e-4367-b1f0-fa63052fbd0f',
       inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011',
       quantity: 2 }]
  elsif params[:model_ids] == ['0a0feaf8-9537-4d39-b5f2-b9411778c90c']
    [{ model_id: '0a0feaf8-9537-4d39-b5f2-b9411778c90c',
       inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011',
       quantity: 2 },
     { model_id: '0a0feaf8-9537-4d39-b5f2-b9411778c90c',
       inventory_pool_id: '5271c14b-e6ef-4252-8d2a-cb0af9ed5a1f',
       quantity: 1 }]
  elsif params[:model_ids] == ['b5925821-6835-4b77-bd16-2ae280113eb6']
    [{ model_id: 'b5925821-6835-4b77-bd16-2ae280113eb6',
       inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011',
       quantity: 1 },
     { model_id: 'b5925821-6835-4b77-bd16-2ae280113eb6',
       inventory_pool_id: '5271c14b-e6ef-4252-8d2a-cb0af9ed5a1f',
       quantity: 1 }]
  elsif
    params[:model_ids] == ['f616b467-80f5-45d7-b708-08c00d506a92'] and
    params[:inventory_pool_ids].to_set == Set['8e484119-76a4-4251-b37b-64847df99e9b',
                                              'a7d2e049-56ac-481a-937e-ee3f613f3cc7']
    [{ model_id: 'f616b467-80f5-45d7-b708-08c00d506a92',
       inventory_pool_id: '8e484119-76a4-4251-b37b-64847df99e9b',
       quantity: 2 },
     { model_id: 'f616b467-80f5-45d7-b708-08c00d506a92',
       inventory_pool_id: 'a7d2e049-56ac-481a-937e-ee3f613f3cc7',
       quantity: 2 }]
  elsif
    params[:model_ids] == ['f616b467-80f5-45d7-b708-08c00d506a92'] and
    params[:inventory_pool_ids] == ['a7d2e049-56ac-481a-937e-ee3f613f3cc7']
    [{ model_id: 'f616b467-80f5-45d7-b708-08c00d506a92',
       inventory_pool_id: 'a7d2e049-56ac-481a-937e-ee3f613f3cc7',
       quantity: 1 }]
  else
    raise "Unknown model IDs: #{params[:model_ids]}"
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
