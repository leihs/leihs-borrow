import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

export default function CategoryBreadcrumbs({ ancestorCats, className, ...restProps }) {
  return (
    <ul className={cx(className, 'ui-category-breadcrumbs category-breadcrumbs list-inline fw-normal')} {...restProps}>
      {ancestorCats.map(({ id, name, url }) => (
        <li key={id}>
          <a href={url}>{name}</a>
        </li>
      ))}
    </ul>
  )
}

CategoryBreadcrumbs.propTypes = {
  ancestorCats: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      url: PropTypes.string.isRequired
    })
  ),
  className: PropTypes.string
}
