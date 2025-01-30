import React from 'react'
import cx from 'classnames'
import PropTypes from 'prop-types'

export default function Stack({ children, divided = false, space = 0, className, ...restProps }) {
  const nonEmptyChildren = [...React.Children.toArray(children).filter(Boolean)]
  const length = nonEmptyChildren.length
  return (
    nonEmptyChildren.map((child, i) => {
      const isLast = i + 1 === length
      return (
        <div key={i} className={cx(isLast ? '' : 'mb-' + space, className)} {...restProps}>
          {i === 0 && divided && divided !== 'between' && divided !== 'bottom' && renderDivider(space)}
          {child}
          {divided && (!isLast || (divided !== 'between' && divided !== 'top')) && renderDivider(space)}
        </div>
      )
    }) || null
  )
}

function renderDivider(space) {
  return <hr className={'page-inset-x-inverse my-' + space} />
}

Stack.propTypes = {
  children: PropTypes.node,
  divided: PropTypes.oneOf([true, false, 'all', 'top', 'bottom', 'between']),
  space: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  className: PropTypes.string
}
