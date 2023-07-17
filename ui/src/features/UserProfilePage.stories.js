import React from 'react'

import { action } from '@storybook/addon-actions'

import PageLayoutMock from '../story-utils/PageLayoutMock'
import UserProfilePage from './UserProfilePage.js'

export default {
  title: 'Feature Components/UserProfilePage',
  component: UserProfilePage,
  parameters: { layout: 'fullscreen' },
  args: { onLogoutClick: action() }
}

export function userProfilePage({ onLogoutClick }) {
  const txt = {
    pageTitle: 'Benutzerkonto',
    sectionUserData: 'Nutzerdaten',
    sectionContracts: 'Verträge',
    sectionDelegations: 'Delegationen',
    logout: 'Abmelden',
    noContracts: 'Keine Verträge vorhanden'
  }
  return (
    <PageLayoutMock>
      <UserProfilePage
        txt={txt}
        user={user}
        delegations={delegations}
        contracts={contracts}
        onLogoutClick={onLogoutClick}
      />
    </PageLayoutMock>
  )
}
userProfilePage.storyName = 'UserProfilePage'

const user = {
  email: 'anna.beispiel@example1.com',
  orgId: '1234567',
  phone: '079 444 44 44',
  name: 'Anna Beispiel',
  badgeId: 'E012XAMPLE',
  organization: 'zhdk.ch',
  secondaryEmail: 'anna@example2.com',
  id: '123456',
  suspensions: [],
  inventoryPools: []
}
const delegations = [
  {
    id: '123',
    name: 'Master Design',
    responsibleName: 'Maria Bernasconi',
    responsibleEmail: 'maria@example3.com'
    // href: 'example' // (not used by app)
  }
]
const contracts = []
