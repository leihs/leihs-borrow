/* eslint-disable react/prop-types */
/* Component for prototype stories! Not to be used in production code */
import React from 'react'
import ListCard from '../../components/ListCard'

export default function ReservationCard({
  quantity,
  modelName,
  inventoryCode,
  poolName,
  startDate,
  endDate,
  durationDays,
  delegationName,
  statusInfo,
  ...restProps
}) {
  return (
    <ListCard {...restProps}>
      <ListCard.Title>
        <div className="d-md-flex gap-5 justify-content-between">
          {quantity}x {modelName} {inventoryCode && ` (${inventoryCode})`}
          {statusInfo && (
            <div className="text-nowrap d-none d-md-block" style={{}}>
              {statusInfo}
            </div>
          )}
        </div>
      </ListCard.Title>
      <ListCard.Body>
        <div>{poolName}</div>
        <div>
          {`${startDate} – ${endDate}`} ({durationDays} Tage)
        </div>
        {delegationName && <div>{delegationName}</div>}
      </ListCard.Body>
      {statusInfo && <ListCard.Foot className="fw-bold d-md-none">{statusInfo}</ListCard.Foot>}
    </ListCard>
  )
}
