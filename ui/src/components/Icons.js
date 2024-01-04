import React from 'react'

export { default as MenuIcon } from '../../static/borrow-app-icons/menu.svg'
export { default as MenuCloseIcon } from '../../static/borrow-app-icons/menu-close.svg'
export { default as BagIcon } from '../../static/borrow-app-icons/cart.svg'
export { default as UserIcon } from '../../static/borrow-app-icons/user.svg'
export { default as PowerOffIcon } from '../../static/borrow-app-icons/power-off.svg'
import AppsIconSvg from '../../static/borrow-app-icons/apps.svg' // see below for re-export

export { default as ArrowIcon } from '../../static/borrow-app-icons/arrow.svg'
export { default as LargeArrowLeftIcon } from '../../static/borrow-app-icons/large-arrow-left.svg'
export { default as LargeArrowRightIcon } from '../../static/borrow-app-icons/large-arrow-right.svg'
export { default as CardArrowIcon } from '../../static/borrow-app-icons/card-arrow.svg'
export { default as CollapserArrowIcon } from '../../static/borrow-app-icons/collapser-arrow.svg'

import SettingsIconSvg from '../../static/borrow-app-icons/settings.svg' // see below for re-export
export { default as CalendarIcon } from '../../static/borrow-app-icons/calendar.svg'
export { default as CalendarCrossIcon } from '../../static/borrow-app-icons/calendar-cross.svg'
export { default as CrossIcon } from '../../static/borrow-app-icons/cross.svg'
export { default as CircleCrossIcon } from '../../static/borrow-app-icons/circle-cross.svg'
export { default as CircleMinusIcon } from '../../static/borrow-app-icons/circle-minus.svg'
export { default as CirclePlusIcon } from '../../static/borrow-app-icons/circle-plus.svg'
export { default as DownloadIcon } from '../../static/borrow-app-icons/download.svg'
export { default as StarIcon } from '../../static/borrow-app-icons/star.svg'
export { default as TemplateIcon } from '../../static/borrow-app-icons/template-picto.svg'

// Icons with default size fix:
export function AppsIcon({ ...props }) {
  return <AppsIconSvg width="20px" height="20px" {...props} />
}
export function SettingsIcon({ ...props }) {
  return <SettingsIconSvg width="24px" height="24px" {...props} />
}
