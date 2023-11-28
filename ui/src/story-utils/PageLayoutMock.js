import React from 'react'
import PageLayout from '../components/PageLayout'
import Topnav from '../components/Topnav'

export default function PageLayoutMock({ children, contained = true, ...overrides }) {
  const pageLayoutProps = {
    topBar: (
      <Topnav
        cartItemCount={3}
        appMenuData={{ children: 'Bereiche...' }}
        userProfileShort="AB"
        desktopUserMenuData={{ children: 'Benutzermenu...' }}
        mainMenuItems={[{ label: 'Nav 1', selected: true }, { label: 'Nav 2' }, { label: 'Nav 3' }]}
      />
    ),
    ...overrides
  }
  return (
    <PageLayout {...pageLayoutProps}>
      {contained ? <PageLayout.ContentContainer>{children}</PageLayout.ContentContainer> : children}
    </PageLayout>
  )
}
