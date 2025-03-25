import React from 'react'
import CircleBadge from './CircleBadge'

export default {
  title: 'Design Components/Content/CircleBadge',
  component: CircleBadge
}

export const circleBadge = () => {
  return (
    <div>
      <h1>CircleBadge</h1>
      <p className="text-muted">The usual number-of-thingwe badge</p>
      <div className="mb-3 d-flex gap-1">
        <CircleBadge>7</CircleBadge>
        <CircleBadge>0</CircleBadge>
        <CircleBadge>9</CircleBadge>
        <CircleBadge>17</CircleBadge>
      </div>
      <p className="text-muted mt-4">
        Aligned with inline text (<code>inline</code> prop)
      </p>
      <div>
        Lorem ipsum <CircleBadge inline>7</CircleBadge> dolor sit amet
      </div>
      <h1>
        Lorem ipsum <CircleBadge inline>7</CircleBadge> dolor sit amet
      </h1>
      <p className="text-muted mt-4">Works with any font size</p>
      <div className="d-flex gap-1">
        <CircleBadge style={{ fontSize: '20px' }}>0</CircleBadge>
        <CircleBadge style={{ fontSize: '11px' }}>0</CircleBadge>
      </div>
      <div className="fs-1">
        Lorem ipsum{' '}
        <CircleBadge inline style={{ fontSize: 'inherit' }}>
          7
        </CircleBadge>{' '}
        dolor sit amet
      </div>
      <p className="text-muted mt-4">Variants: primary, secondary, warning, danger</p>
      <div className="d-flex gap-1">
        <CircleBadge variant="primary">0</CircleBadge>
        <CircleBadge variant="secondary">0</CircleBadge>
        <CircleBadge variant="warning">0</CircleBadge>
        <CircleBadge variant="danger">0</CircleBadge>
      </div>
    </div>
  )
}
circleBadge.storyName = 'CircleBadge'
