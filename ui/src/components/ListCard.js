import React from 'react'
import cx from 'classnames'
import { CardArrowIcon } from './Icons'
import Stack from './Stack'

const BASE_CLASS = 'ui-list-card'

export default function ListCard({ onClick, href, img, children, className, oneLine, ...restProps }) {
  const clickable = !!(onClick || href)

  const wrapperClass = cx(
    'list-card',
    { 'list-card--clickable': clickable, 'list-card--one-line': oneLine },
    className,
    BASE_CLASS
  )

  return (
    <div className={wrapperClass} onClick={onClick} {...restProps}>
      {img && <div className="list-card__image">{img}</div>}

      <div className="list-card__content">{children}</div>

      {clickable && (
        <a
          href={href}
          className={cx('list-card__arrow', {
            'stretched-link': !!href
          })}
        >
          <CardArrowIcon />
        </a>
      )}
    </div>
  )
}

ListCard.Stack = function ListCardStack({ children, className, ...restProps }) {
  return (
    <Stack divided space="0" className={className} {...restProps}>
      {children}
    </Stack>
  )
}

ListCard.Title = function ListCardTitle({ children, className, ...restProps }) {
  return (
    <div className={cx('list-card__title', className)} data-test-id="title" {...restProps}>
      {children}
    </div>
  )
}
ListCard.Body = function ListCardBody({ children, className, ...restProps }) {
  return (
    <div className={cx('list-card__body', className)} data-test-id="body" {...restProps}>
      {children}
    </div>
  )
}
ListCard.Foot = function ListCardFoot({ children, className, ...restProps }) {
  return (
    <div className={cx('list-card__foot', className)} data-test-id="foot" {...restProps}>
      {children}
    </div>
  )
}
