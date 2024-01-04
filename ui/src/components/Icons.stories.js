import React from 'react'
import * as allIcons from './Icons'

export default {
  title: 'Design Components/Content/Icons'
}

function renderIcon(Icon, name) {
  return (
    <div className="d-flex align-items-end mb-3">
      <div className="pe-3 text-center">
        <div style={{ width: '2rem' }}>
          <Icon />
        </div>
      </div>
      <div className="small">
        <pre className="mb-0">{`<${name} />`}</pre>
      </div>
    </div>
  )
}

export const icons = () => {
  return (
    <div>
      <h1>Icons</h1>
      <h2>Topnav</h2>
      {renderIcon(allIcons.MenuIcon, 'MenuIcon')}
      {renderIcon(allIcons.MenuCloseIcon, 'MenuCloseIcon')}
      {renderIcon(allIcons.BagIcon, 'BagIcon')}
      {renderIcon(allIcons.UserIcon, 'UserIcon')}
      {renderIcon(allIcons.PowerOffIcon, 'PowerOffIcon')}
      {renderIcon(allIcons.AppsIcon, 'AppsIcon')}

      <h2 className="mt-4">Arrows</h2>
      {renderIcon(allIcons.ArrowIcon, 'ArrowIcon')}
      {renderIcon(allIcons.LargeArrowLeftIcon, 'LargeArrowLeftIcon')}
      {renderIcon(allIcons.LargeArrowRightIcon, 'LargeArrowRightIcon')}
      {renderIcon(allIcons.CardArrowIcon, 'CardArrowIcon')}
      {renderIcon(allIcons.CollapserArrowIcon, 'CollapserArrowIcon')}

      <h2 className="mt-4">Misc</h2>
      {renderIcon(allIcons.SettingsIcon, 'SettingsIcon')}
      {renderIcon(allIcons.CalendarIcon, 'CalendarIcon')}
      {renderIcon(allIcons.CalendarCrossIcon, 'CalendarCrossIcon')}
      {renderIcon(allIcons.CrossIcon, 'CrossIcon')}
      {renderIcon(allIcons.CircleCrossIcon, 'CircleCrossIcon')}
      {renderIcon(allIcons.CircleMinusIcon, 'CircleMinusIcon')}
      {renderIcon(allIcons.CirclePlusIcon, 'CirclePlusIcon')}
      {renderIcon(allIcons.DownloadIcon, 'DownloadIcon')}
      {renderIcon(allIcons.StarIcon, 'StarIcon')}

      <h2 className="mt-4">Special</h2>
      {renderIcon(allIcons.TemplateIcon, 'TemplateIcon')}
    </div>
  )
}

export const iconStyling = () => {
  const StarIcon = allIcons.StarIcon
  return (
    <div>
      <h1>Icons</h1>
      <h2 className="text-muted">CSS inheritance</h2>
      <p className="text-primary">
        <StarIcon /> color inherited from this paragraph
      </p>
      <p>
        <span style={{ width: '12px', display: 'inline-block' }}>
          <StarIcon style={{ width: '100%' }} />
        </span>{' '}
        shrink by CSS width
      </p>
      <h2 className="text-muted">Attributes (restProps)</h2>
      <p>
        <StarIcon style={{ color: 'red' }} /> colored by style attribute
      </p>
      <p>
        <StarIcon height="12px" width="12px" />
        <StarIcon />
        <StarIcon height="18px" width="18px" /> shrink/grow by width and/or height attribute
      </p>
      <p className="text-muted">
        Note: Normally no resizing is necessary since the SVGs have an inherent size as needed by the design.
      </p>
    </div>
  )
}
