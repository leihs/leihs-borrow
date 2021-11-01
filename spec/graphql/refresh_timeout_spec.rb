require 'spec_helper'
require_relative 'graphql_helper'

describe 'refresh timeout' do
  let(:user) do
    FactoryBot.create(
      :user,
      id: '8c360361-f70c-4b31-a271-b4050d4b9d26'
    )
  end

  let(:inventory_pool) do
    FactoryBot.create(
      :inventory_pool,
      id: '00843766-b48d-4a7d-89cc-565ced81bbf9'
    )
  end

  let(:model) do
    FactoryBot.create(:leihs_model,
                      id: 'db3197f4-7fef-4139-83e1-09f79abfa691')
  end

  let(:m) do 
    <<-GRAPHQL
      mutation {
        refreshTimeout {
          unsubmittedOrder {
            reservations {
              id
            }
          }
        }
      }
    GRAPHQL
  end

  let(:q) do
    <<-GRAPHQL
      query {
        currentUser {
          user {
            unsubmittedOrder {
              reservations {
                id
              }
            }
            draftOrder {
              reservations(orderBy: [{attribute: ID, direction: ASC}]) {
                id
              }
            }
          }
        }
      }
    GRAPHQL
  end

  before(:example) do
    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool,
                      user: user)
    2.times do
      model.add_item(
        FactoryBot.create(:item,
                          is_borrowable: true,
                          responsible: inventory_pool)
      )
    end
  end

  it 'change to draft when invalid start date' do
    # no workdays for pool => fallback to reservation_advance_days = 0
    r_invalid = FactoryBot.create(:reservation,
                                  id: '0a3ea476-f1e9-42e8-943f-35f7eefdad90',
                                  leihs_model: model,
                                  inventory_pool: inventory_pool,
                                  start_date: Date.yesterday,
                                  end_date: Date.tomorrow,
                                  user: user)
    r_ok = FactoryBot.create(:reservation,
                             id: 'a478afe1-798f-4687-a525-ca4d26606d63',
                             leihs_model: model,
                             inventory_pool: inventory_pool,
                             start_date: Date.today,
                             end_date: Date.tomorrow,
                             user: user)

    m_result = query(m, user.id).deep_symbolize_keys
    expect(m_result[:data]).to eq({
      refreshTimeout: {
        unsubmittedOrder: nil
      }
    })
    expect(m_result[:errors]).to be_nil

    q_result = query(q, user.id).deep_symbolize_keys
    expect(q_result[:data]).to eq({
      currentUser: {
        user: {
          draftOrder: { 
            reservations: [{ id: r_invalid.id },
                           { id: r_ok.id }]
          },
          unsubmittedOrder: nil
        }
      }
    })
    expect(q_result[:errors]).to be_nil
  end

  context 'valid start date' do
    let(:m) do 
      <<-GRAPHQL
        mutation {
          refreshTimeout {
            unsubmittedOrder {
              reservations {
                id
              }
              invalidReservationIds
              validUntil
            }
          }
        }
      GRAPHQL
    end

    context 'extend timeout' do
      it 'no timeout' do
        @r = FactoryBot.create(:reservation,
                              id: 'c7146ba8-97b7-422f-9aca-c5d85f4675cf',
                              leihs_model: model,
                              inventory_pool: inventory_pool,
                              start_date: Date.today,
                              end_date: Date.tomorrow,
                              user: user)

        # NOTE: see after hook further below
      end

      it 'timed-out, but model still available' do
        dt = DateTime.now - 31.minutes
        @r = FactoryBot.create(:reservation,
                               id: 'e9bd4dcf-a20f-45eb-bde7-727526abcd92',
                               leihs_model: model,
                               inventory_pool: inventory_pool,
                               start_date: Date.today,
                               end_date: Date.tomorrow,
                               user: user,
                               created_at: dt,
                               updated_at: dt)

        # NOTE: see after hook further below
      end

      after(:each) do
        m_result = query(m, user.id).deep_symbolize_keys

        valid_until =
          DateTime.parse(
            m_result
          .dig(:data, :refreshTimeout, :unsubmittedOrder)
          .delete(:validUntil) 
          )

        expect(m_result[:data]).to eq({
          refreshTimeout: {
            unsubmittedOrder: {
              reservations: [{ id: @r.id }],
              invalidReservationIds: []
            }
          }
        })

        expect(DateTime.now).to be < valid_until
        expect(DateTime.now + 30.minutes).not_to be < valid_until

        expect(m_result[:errors]).to be_nil
      end
    end

    it 'don\'t extend timeout' do
      dt = (DateTime.now - 31.minutes).utc
      model = FactoryBot.create(:leihs_model,
                                id: 'a95ee25f-37cb-4c85-8efd-40cead86396e')
      r = FactoryBot.create(:reservation,
                            id: '0bb651b5-4a55-4bea-ab0c-0ad4d00673aa',
                            leihs_model: model,
                            inventory_pool: inventory_pool,
                            start_date: Date.today,
                            end_date: Date.tomorrow,
                            user: user,
                            created_at: dt,
                            updated_at: dt)

      m_result = query(m, user.id).deep_symbolize_keys

      valid_until =
        DateTime.parse(
          m_result
        .dig(:data, :refreshTimeout, :unsubmittedOrder)
        .delete(:validUntil) 
        )

      expect(m_result[:data]).to eq({
        refreshTimeout: {
          unsubmittedOrder: {
            reservations: [{ id: r.id }],
            invalidReservationIds: [r.id]
          }
        }
      })

      expect(valid_until.utc).to eq (dt + 30.minutes).change(usec: 0)

      expect(m_result[:errors]).to be_nil
    end
  end
end
