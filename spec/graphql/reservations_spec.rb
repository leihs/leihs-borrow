require 'spec_helper'
require_relative 'graphql_helper'

describe 'reservations' do
  let(:user) do
    FactoryBot.create(
      :user,
      id: '890c7116-7649-4620-aa60-ff3a814c6ca9'
    )
  end

  let(:inventory_pool_1) do
    FactoryBot.create(
      :inventory_pool,
      id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011'
    )
  end

  let(:inventory_pool_2) do
    FactoryBot.create(
      :inventory_pool,
      id: '5271c14b-e6ef-4252-8d2a-cb0af9ed5a1f'
    )
  end

  let(:model_1) do
    FactoryBot.create(:leihs_model,
                      id: '906ac7a7-1f1e-4367-b1f0-fa63052fbd0f')
  end

  let(:model_2) do
    FactoryBot.create(:leihs_model,
                      id: '0a0feaf8-9537-4d39-b5f2-b9411778c90c')
  end

  let(:model_3) do
    FactoryBot.create(:leihs_model,
                      id: 'b5925821-6835-4b77-bd16-2ae280113eb6')
  end

  before(:example) do
    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_1,
                      user: user)
    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_2,
                      user: user)
  end

  context 'create' do
    let(:q) do
      <<-GRAPHQL
        mutation(
          $modelId: UUID!,
          $startDate: Date!,
          $endDate: Date!,
          $quantity: Int!,
        ) {
          createReservation(
            modelId: $modelId,
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

    context 'within one pool' do
      it 'with implicit inventory pool ids' do
        2.times do
          model_1.add_item(
            FactoryBot.create(:item,
                              is_borrowable: true,
                              responsible: inventory_pool_1)
          )
        end

        vars = {
          modelId: model_1.id,
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

      it 'with explicit inventory pool ids' do
        q = <<-GRAPHQL
          mutation(
            $modelId: UUID!,
            $startDate: Date!,
            $endDate: Date!,
            $quantity: Int!,
            $inventoryPoolIds: [UUID!]
          ) {
            createReservation(
              modelId: $modelId,
              startDate: $startDate,
              endDate: $endDate,
              quantity: $quantity,
              inventoryPoolIds: $inventoryPoolIds
            ) {
              id
              createdAt
              updatedAt
            }
          }
        GRAPHQL

        model_1.add_item(
          FactoryBot.create(:item,
                            is_borrowable: true,
                            responsible: inventory_pool_1)
        )

        vars = {
          modelId: model_1.id,
          startDate: Date.tomorrow.strftime,
          endDate: (Date.tomorrow + 1.day).strftime,
          quantity: 1,
          inventoryPoolIds: [inventory_pool_1.id]
        }

        result = query(q, user.id, vars).deep_symbolize_keys

        reservations = result.dig(:data, :createReservation)
        expect(reservations.count).to eq 1
      end

      context 'throws error' do
        it 'model not reservable' do
          vars = {
            modelId: model_1.id,
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
            model_1.add_item(
              FactoryBot.create(:item,
                                is_borrowable: true,
                                responsible: inventory_pool_1)
            )
          end

          vars = {
            modelId: model_1.id,
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

    context 'within 2 pools' do
      it 'works' do
        2.times do
          model_2.add_item(
            FactoryBot.create(:item,
                              is_borrowable: true,
                              responsible: inventory_pool_1)
          )
        end

        model_2.add_item(
          FactoryBot.create(:item,
                            is_borrowable: true,
                            responsible: inventory_pool_2)
        )

        vars = {
          modelId: model_2.id,
          startDate: Date.tomorrow.strftime,
          endDate: (Date.tomorrow + 1.day).strftime,
          quantity: 3
        }

        result = query(q, user.id, vars).deep_symbolize_keys

        reservations = result.dig(:data, :createReservation)
        expect(reservations.count).to eq 3
      end

      context 'throws error' do
        it 'quantity not available' do
          model_3.add_item(
            FactoryBot.create(:item,
                              is_borrowable: true,
                              responsible: inventory_pool_1)
          )
          model_3.add_item(
            FactoryBot.create(:item,
                              is_borrowable: true,
                              responsible: inventory_pool_2)
          )

          vars = {
            modelId: model_3.id,
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
