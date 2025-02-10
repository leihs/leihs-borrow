import React, { useState } from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import { ArrowIcon } from './Icons'

function TruncateText({ maxHeight, translations, className, children }) {
  const [isExpanded, setIsExpanded] = useState(false)

  const toggleExpand = () => {
    setIsExpanded(!isExpanded)
  }

  return (
    <>
      <div
        className={cx('position-relative overflow-hidden', className)}
        style={{ maxHeight: isExpanded ? 'none' : maxHeight }}
      >
        <div className={cx('truncate-text', isExpanded ? 'truncate-text--expanded' : 'truncate-text--collapsed')}>
          {children}
          {isExpanded && (
            <button className="btn btn-link p-0 text-body fw-bold text-decoration-none" onClick={toggleExpand}>
              <ArrowIcon className="truncate-text__arrow-up" />
              {translations?.hide || 'hide'}
            </button>
          )}
        </div>
        {!isExpanded && (
          <div className="position-absolute bottom-0 w-100">
            <div className={cx('truncate-text__gradient-overlay')}></div>
          </div>
        )}
      </div>
      {!isExpanded && (
        <button className="btn btn-link p-0 text-body fw-bold text-decoration-none" onClick={toggleExpand}>
          <ArrowIcon className="truncate-text__arrow-right mr-1" />
          {translations?.more || 'read more'}
        </button>
      )}
    </>
  )
}

TruncateText.propTypes = {
  translations: PropTypes.shape({
    more: PropTypes.string,
    hide: PropTypes.string
  }),
  maxHeight: PropTypes.string,
  children: PropTypes.node,
  className: PropTypes.string
}

export default TruncateText
