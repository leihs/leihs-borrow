import React from 'react'
import cx from 'classnames'
import { DownloadIcon } from './Icons'

export default function DownloadLink({ children, href, className, ...restProps }) {
  const linkProps = { href, className: cx('d-flex', className), ...restProps }
  return (
    <a {...linkProps}>
      <DownloadIcon style={{ marginRight: '10px', marginTop: '2px', flex: '16px 0 0' }} />
      <div className="fw-bold">{children}</div>
    </a>
  )
}
