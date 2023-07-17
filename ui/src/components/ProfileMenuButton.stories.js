import React from 'react'
import { linkTo } from '@storybook/addon-links'
import ProfileMenuButton from './ProfileMenuButton'
import Menu from './Menu'

export default {
  title: 'Design Components/Layout/ProfileMenuButton',
  component: ProfileMenuButton
}

export function profileMenuButton() {
  return (
    <>
      <h1>ProfileMenuButton</h1>

      <p className="text-muted">
        Can be used like a <code>`Menu.Button`</code>, but has a special design:
      </p>
      <Menu>
        <Menu.Group>
          <ProfileMenuButton profile={user} />
          <ProfileMenuButton profile={delegations[0]} isSelected />
          <ProfileMenuButton profile={delegations[1]} />
        </Menu.Group>
      </Menu>

      <p></p>
      <p className="text-muted">For an example with more context see:</p>
      <p className="text-muted">
        <button className="btn btn-light btn-sm" onClick={linkTo('Prototypes/Profile Switch')}>
          Prototypes &gt; Profile Switch
        </button>
      </p>
    </>
  )
}

profileMenuButton.storyName = 'ProfileMenuButton'

export function loadingState() {
  return (
    <>
      <h1>ProfileMenuButton - Loading state</h1>
      <Menu>
        <Menu.Group>
          <ProfileMenuButton profile={user} />
          <ProfileMenuButton profile={delegations[0]} isSelected />
          <ProfileMenuButton profile={delegations[1]} isLoading />
        </Menu.Group>
      </Menu>{' '}
    </>
  )
}

const user = {
  id: 'U.001',
  profileName: 'Anna Beispiel (persönlich)',
  name: 'Anna Beispiel',
  shortName: 'AB'
}
const delegations = [
  {
    id: 'D.001',
    profileName: 'Projektteam 1',
    shortName: 'P1'
  },
  {
    id: 'D.002',
    profileName: 'Projektteam 2',
    shortName: 'P2'
  }
]
