import React from 'react'
import Icon, { iconPowerOff, iconTemplate, iconUser } from './Icons'

// Icon export to be consumed from CLJS (FIXME - this is quite weird)

export function TemplateIcon(props) {
  return <Icon icon={iconTemplate} {...props} />
}

export function UserIcon(props) {
  return <Icon icon={iconUser} {...props} />
}

export function PowerOffIcon(props) {
  return <Icon icon={iconPowerOff} {...props} />
}
