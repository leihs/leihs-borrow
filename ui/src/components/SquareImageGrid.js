import React from 'react'
import cx from 'classnames'
import { StarIcon } from './Icons'
import SquareImage from './SquareImage'

export default function SquareImageGrid({ list = [], className, itemClassName, ...restProps }) {
  return (
    <div className={cx('ui-square-image-grid row g-0 d-flex flex-wrap mx-n2', className)} {...restProps}>
      {list.map(({ id, href, imgSrc, placeholder, caption, subCaption, isDimmed, isFavorited }) => (
        <div key={id} className="ui-square-image-grid-item col-6 col-sm-3" style={{ opacity: isDimmed ? 0.35 : 1 }}>
          <div
            className={cx('max-w-sm overflow-hidden bg-white mx-2 mb-3 position-relative', itemClassName)}
            style={{ opacity: 1 }}
          >
            {isFavorited && (
              <div className="position-absolute square-image-grid__favorite-marker" title="Favorit">
                <StarIcon />
              </div>
            )}
            <SquareImage href={href} imgSrc={imgSrc} placeholder={placeholder} className="rounded border" />
            <div className="pt-2">
              <a className="stretched-link" href={href}>
                <div className="ui-caption small fw-bold">{caption}</div>
                <div className="ui-sub-caption small fw-normal">{subCaption}</div>
              </a>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
