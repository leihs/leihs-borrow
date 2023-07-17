import React from 'react'
import cx from 'classnames'

export default function PropertyTable({ properties, className, style, ...restProps }) {
  return (
    <table className={className} style={{ tableLayout: 'fixed', ...style }} {...restProps}>
      <tbody>
        {(properties || []).map(({ key, value }, i) => {
          const paddingClassName = i === 0 ? '' : 'pt-2'
          return (
            <tr key={i} className="align-top">
              <td className={cx('fw-normal pe-3 text-break', paddingClassName)} style={{ width: '33%' }}>
                {key}
              </td>
              <td className={cx('fw-bold text-break', paddingClassName)} style={{ width: '66%' }}>
                {value}
              </td>
            </tr>
          )
        })}
      </tbody>
    </table>
  )
}
