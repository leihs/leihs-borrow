require 'active_support/all'
require 'json'
require 'pry'
require 'sinatra'

require_relative 'spec/models/filter_available.rb'

set :show_exceptions, false

get '/status' do
  'OK'
end

get '/borrow/models/availability' do
  if params[:model_ids].to_set ==
      Set['7efd48dc-676f-4438-9d1b-d0774b6704b7',
          '5577cbcf-fdc4-4cfc-bdb9-435d75522c1d',
          '87420e5a-c916-42f6-94ac-dd31ea32afb2']
    [{ model_id: '7efd48dc-676f-4438-9d1b-d0774b6704b7',
       inventory_pool_id: 'ab61cf01-08ce-4d9b-97d3-8dcd8360605a',
       quantity: 0 },
     { model_id: '5577cbcf-fdc4-4cfc-bdb9-435d75522c1d',
       inventory_pool_id: 'ab61cf01-08ce-4d9b-97d3-8dcd8360605a',
       quantity: 1 },
     { model_id: '5577cbcf-fdc4-4cfc-bdb9-435d75522c1d',
       inventory_pool_id: '6ce92dd1-cf47-4942-97a1-6bc5b495b425',
       quantity: 1 },
     { model_id: '87420e5a-c916-42f6-94ac-dd31ea32afb2',
       inventory_pool_id: 'ab61cf01-08ce-4d9b-97d3-8dcd8360605a',
       quantity: -2 },
     { model_id: '87420e5a-c916-42f6-94ac-dd31ea32afb2',
       inventory_pool_id: '6ce92dd1-cf47-4942-97a1-6bc5b495b425',
       quantity: 1 }]
  elsif params[:model_ids] == ['906ac7a7-1f1e-4367-b1f0-fa63052fbd0f']
    [{ model_id: '906ac7a7-1f1e-4367-b1f0-fa63052fbd0f',
       inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011',
       quantity: 2 }]
  elsif params[:model_ids] == ['98d398e7-08b3-49d4-807c-42a3eac07de9'] and
    params[:inventory_pool_ids] == ['8633ce17-37da-4802-a377-66ca78291d0a']
    [{ model_id: '98d398e7-08b3-49d4-807c-42a3eac07de9',
       inventory_pool_id: '8633ce17-37da-4802-a377-66ca78291d0a',
       quantity: 1 }]
  elsif params[:model_ids] == ['98d398e7-08b3-49d4-807c-42a3eac07de9'] and
    params[:inventory_pool_ids] == ['4e2f1362-0891-4df7-b760-16a2a8d3373f']
    [{ model_id: '98d398e7-08b3-49d4-807c-42a3eac07de9',
       inventory_pool_id: '4e2f1362-0891-4df7-b760-16a2a8d3373f',
       quantity: 1 }]
  elsif params[:model_ids] == ['3c83f2b8-259d-4aa6-99f7-c29c81f31b54'] and
    params[:inventory_pool_ids] == ['de1ab6c2-5c85-45fb-aebf-527b6096411c']
    [{ model_id: '3c83f2b8-259d-4aa6-99f7-c29c81f31b54',
       inventory_pool_id: 'de1ab6c2-5c85-45fb-aebf-527b6096411c',
       quantity: 1 }]
  elsif params[:model_ids] == ['fd3cef3d-578c-4409-b814-eb20a46da21d'] and
    params[:inventory_pool_ids] == ['8633ce17-37da-4802-a377-66ca78291d0a']
    [{ model_id: 'fd3cef3d-578c-4409-b814-eb20a46da21d',
       inventory_pool_id: '8633ce17-37da-4802-a377-66ca78291d0a',
       quantity: 1 }]
  elsif params[:model_ids] == ['34d2f947-1ec1-474c-9afd-4b71b6c1d966'] and
    params[:inventory_pool_ids] == ['4e2f1362-0891-4df7-b760-16a2a8d3373f']
    [{ model_id: '34d2f947-1ec1-474c-9afd-4b71b6c1d966',
       inventory_pool_id: '4e2f1362-0891-4df7-b760-16a2a8d3373f',
       quantity: 1 }]
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
       quantity: 2 }]
  elsif
    params[:model_ids] == ['f616b467-80f5-45d7-b708-08c00d506a92'] and
      params[:inventory_pool_ids] == ['8e484119-76a4-4251-b37b-64847df99e9b']
    [{ model_id: 'f616b467-80f5-45d7-b708-08c00d506a92',
       inventory_pool_id: '8e484119-76a4-4251-b37b-64847df99e9b',
       quantity: 2 }]
  else
    Mock::Spec::FilterAvailable.availability(params) or
      raise("Unknown model IDs: #{params[:model_ids]}")
  end.to_json
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
