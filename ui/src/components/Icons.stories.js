import React from 'react'
import Icon, { allIcons, iconStar } from './Icons'

export default {
  title: 'Design Components/Content/Icons'
}

export const icons = () => {
  return (
    <div>
      <h1>Icons</h1>
      {allIcons.map((icon, i) => {
        return (
          <div key={i} className="d-flex align-items-end mb-3">
            <div className="pe-3 text-center">
              <div style={{ width: '2rem' }}>
                <Icon icon={icon} />
              </div>
            </div>
            <div className="small">
              <pre className="mb-0">{`<Icon icon={${icon.name}} />`}</pre>
            </div>
          </div>
        )
      })}
    </div>
  )
}

export const iconStyling = () => {
  return (
    <div>
      <h1>Icons</h1>
      <h2 className="text-muted">CSS inheritance</h2>
      <p className="text-primary">
        <Icon icon={iconStar} /> color inherited from this paragraph
      </p>
      <p>
        <div style={{ width: '12px', display: 'inline-block' }}>
          <Icon icon={iconStar} style={{ width: '100%' }} />
        </div>{' '}
        shrink by CSS width
      </p>
      <h2 className="text-muted">Attributes (restProps)</h2>
      <p>
        <Icon icon={iconStar} style={{ color: 'red' }} /> colored by style attribute
      </p>
      <p>
        <Icon icon={iconStar} height="12px" width="12px" />
        <Icon icon={iconStar} />
        <Icon icon={iconStar} height="18px" width="18px" /> shrink/grow by width and/or height attribute
      </p>
      <p className="text-muted">
        Note: Normally no resizing is necessary since the SVGs have an inherent size as needed by the design.
      </p>
    </div>
  )
}
