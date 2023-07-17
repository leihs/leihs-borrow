import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function ProgressInfo({ title, info, totalCount, doneCount, small, className, ...restProps }) {
  return (
    <div
      className={cx(
        'ui-progress-info progress-info d-flex flex-column gap-1 fw-bold',
        className,
        small ? 'progress-info--small' : ''
      )}
      {...restProps}
    >
      <div className={cx(small ? 'small' : '')}>{title}</div>
      {totalCount && <Progressbar totalCount={totalCount} doneCount={doneCount} />}
      {info && <div className={cx(small ? 'very-small' : 'small')}>{info}</div>}
    </div>
  )
}

ProgressInfo.propTypes = {
  title: PropTypes.oneOfType([PropTypes.string, PropTypes.object]),
  totalCount: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  doneCount: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  info: PropTypes.node,
  small: PropTypes.bool,
  className: PropTypes.string
}

function Progressbar({ totalCount, doneCount }) {
  let done = Number(doneCount / totalCount)
  done = Number.isFinite(done) ? done : 0

  // Percentage with 2 fraction digits e.g. "33.33%"
  var donePercents = done.toLocaleString('en-US', {
    style: 'percent',
    minimumFractionDigits: 2
  })

  return (
    <div className="progress" style={{ height: '0.5rem' }}>
      <div
        className="progress-bar rounded"
        role="progressbar"
        style={{ width: donePercents }}
        aria-valuenow={doneCount}
        aria-valuemin="0"
        aria-valuemax={totalCount}
      ></div>
    </div>
  )
}
