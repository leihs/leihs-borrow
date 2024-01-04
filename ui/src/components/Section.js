import React, { useState } from 'react'
import cx from 'classnames'
import { CollapserArrowIcon } from './Icons'

export default function Section({
  title,
  children,
  collapsible,
  initialCollapsed,
  onToggleCollapse,
  className,
  ...restProps
}) {
  const [collapsed, setCollapsed] = useState(initialCollapsed)

  const toggleCollapse = nextCollapsed => {
    setCollapsed(nextCollapsed)
    onToggleCollapse && onToggleCollapse(nextCollapsed)
  }

  return (
    <ControlledSection
      title={title}
      collapsible={collapsible}
      collapsed={collapsed}
      onToggleCollapse={toggleCollapse}
      className={className}
      {...restProps}
    >
      {children}
    </ControlledSection>
  )
}

function ControlledSection({ title, children, collapsible, collapsed, onToggleCollapse, className, ...restProps }) {
  const toggle = () => {
    if (collapsible) {
      onToggleCollapse(!collapsed)
    }
  }

  return (
    <section className={cx(className)} {...restProps}>
      {(title || collapsible) && (
        <h2 className={cx('section-title', 'fw-normal')}>
          <span
            onClick={toggle}
            className={cx({
              'section-title__collapser': collapsible,
              'ui-section-expander': collapsible && collapsed,
              'ui-section-collapser': collapsible && !collapsed
            })}
            role={collapsible ? 'button' : ''}
          >
            {title}
            {collapsible && (
              <CollapserArrowIcon
                className={cx('section-title__arrow-icon', {
                  'section-title__arrow-icon--collapsed': collapsed
                })}
              />
            )}
          </span>
        </h2>
      )}

      {collapsed || <div>{children}</div>}
    </section>
  )
}

Section.Controlled = ControlledSection
