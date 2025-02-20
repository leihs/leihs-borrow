/* eslint-disable react/prop-types */
/* Component for prototype stories! Not to be used in production code */
import React from 'react'
import ListCard from '../../components/ListCard'
import ProgressInfo from '../../components/ProgressInfo'

export default function OrderCard({ order, orderLink, ...restProps }) {
  return (
    <div>
      <ListCard href={orderLink} {...restProps}>
        <div className="d-md-flex">
          <div style={{ flex: '1 1 53%' }} className="pe-4">
            <ListCard.Title>
              <a href={orderLink}>{order.title}</a>
            </ListCard.Title>
            <ListCard.Body>
              {`${order.startDate} – ${order.endDate}`}, {order.modelCount}{' '}
              {order.modelCount === 1 ? 'Gegenstand' : 'Gegenstände'}
            </ListCard.Body>
          </div>
          <div style={{ flex: '1 1 47%' }}>
            <ListCard.Foot className="p-md-0 pe-md-3">
              <div className="d-grid gap-2">
                {order.stateGroups.map((stateGroup, i) => (
                  <ProgressInfo key={i} {...stateGroup} small={true} />
                ))}
              </div>
            </ListCard.Foot>
          </div>
        </div>
      </ListCard>
    </div>
  )
}
