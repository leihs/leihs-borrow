require 'spec_helper'
require_relative 'graphql_helper'

describe 'reservations' do
  let(:user) do
    FactoryBot.create(
      :user,
      id: '890c7116-7649-4620-aa60-ff3a814c6ca9'
    )
  end

  let(:inventory_pool) do
    FactoryBot.create(
      :inventory_pool,
      id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011'
    )
  end

  let(:model) do
    FactoryBot.create(:leihs_model,
                      id: '906ac7a7-1f1e-4367-b1f0-fa63052fbd0f')
  end

  before(:example) do
    FactoryBot.create(:access_right,
                      inventory_pool: inventory_pool,
                      user: user)
  end

  context 'create' do
    let(:q) do
      <<-GRAPHQL
        mutation(
          $modelId: UUID!,
          $inventoryPoolId: UUID!,
          $startDate: String!,
          $endDate: String!,
          $quantity: Int!,
        ) {
          createReservation(
            modelId: $modelId,
            inventoryPoolId: $inventoryPoolId,
            startDate: $startDate,
            endDate: $endDate,
            quantity: $quantity
          ) {
            id
            createdAt
            updatedAt
          }
        }
      GRAPHQL
    end

    it 'works' do
      2.times do 
        model.add_item(
          FactoryBot.create(:item,
                            is_borrowable: true,
                            responsible: inventory_pool)
        )
      end

      vars = {
        modelId: model.id,
        inventoryPoolId: inventory_pool.id,
        startDate: Date.tomorrow.strftime,
        endDate: (Date.tomorrow + 1.day).strftime,
        quantity: 2
      }

      result = query(q, user.id, vars).deep_symbolize_keys

      reservations = result.dig(:data, :createReservation)
      expect(reservations.count).to eq 2

      now = Time.now

      reservations.each do |reservation|
        # NOTE: The timestamp stored in DB is calculated according
        # to `settings.time_zone` and stored without time zone,
        # but representing UTC.
        # Clojure backend formats the timestamp according to
        # ISO 8601 while adding UTC time zone to it.
        # At the end of the day, the time difference in minutes
        # between now and `created_at` / `updated_at` should be
        # 0, which means that all the time zone stuff was considered
        # correctly accross all the layers.
        created_at = DateTime.parse(reservation[:createdAt])
        diff_in_minutes = ((now - created_at) / 1.minutes).round
        expect(diff_in_minutes).to eq 0

        updated_at = DateTime.parse(reservation[:updatedAt])
        diff_in_minutes = ((now - updated_at) / 1.minutes).round
        expect(diff_in_minutes).to eq 0
      end
    end

    context 'throws error' do
      it 'model not reservable' do
        vars = {
          modelId: model.id,
          inventoryPoolId: inventory_pool.id,
          startDate: Date.tomorrow.strftime,
          endDate: (Date.tomorrow + 1.day).strftime,
          quantity: 1
        }

        result = query(q, user.id, vars).deep_symbolize_keys
        expect(result[:data][:createReservation]).to be_nil
        expect(result[:errors].first[:message]).to eq \
          'Model either does not exist or is not reservable by the user.'
      end

      it 'quantity not available' do
        2.times do 
          model.add_item(
            FactoryBot.create(:item,
                              is_borrowable: true,
                              responsible: inventory_pool)
          )
        end

        vars = {
          modelId: model.id,
          inventoryPoolId: inventory_pool.id,
          startDate: Date.tomorrow.strftime,
          endDate: (Date.tomorrow + 1.day).strftime,
          quantity: 3
        }

        result = query(q, user.id, vars).deep_symbolize_keys
        expect(result[:data][:createReservation]).to be_nil
        expect(result[:errors].first[:message]).to eq \
          'The desired quantity is not available.'
      end
    end
  end

  it 'delete' do
    delegation = FactoryBot.create(:delegation)
    delegation.add_delegation_user(user)

    r1 = FactoryBot.create(:reservation, user: user)
    r2 = FactoryBot.create(:reservation, user: delegation)
    r3 = FactoryBot.create(:reservation)

    q = <<-GRAPHQL
      mutation($ids: [UUID!]) {
        deleteReservations(
          ids: $ids
        )
      }
    GRAPHQL

    vars = { ids: [r1.id, r2.id, r3.id] }

    result = query(q, user.id, vars).deep_symbolize_keys
    expect(result[:data][:deleteReservations].to_set).to eq(Set[r1.id, r2.id])
    expect(result[:errors]).to be_nil
  end
end
