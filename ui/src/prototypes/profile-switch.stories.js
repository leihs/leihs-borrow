import React, { useState } from 'react'

import Menu from '../components/Menu'
import ProfileMenuButton from '../components/ProfileMenuButton'

export default {
  title: 'Prototypes/Profile Switch'
}

export function profileSwitch() {
  const [currentProfile, setCurrentProfile] = useState(user)
  const [changingToProfileId, setChangingToProfileId] = useState()
  const changeProfile = d => {
    setChangingToProfileId(d.id)
    setTimeout(() => {
      setCurrentProfile(d)
      setChangingToProfileId(null)
    }, 1000)
  }

  return (
    <>
      <h1>Profile Switch</h1>
      <Menu>
        <Menu.Group title="Profil wechseln">
          <ProfileMenuButton
            profile={user}
            isSelected={user.id === currentProfile.id}
            isLoading={user.id === changingToProfileId}
            onClick={() => changeProfile(user)}
          />

          {delegations.map((d, i) => (
            <React.Fragment key={i}>
              <ProfileMenuButton
                profile={d}
                isSelected={d.id === currentProfile.id}
                isLoading={d.id === changingToProfileId}
                onClick={() => changeProfile(d)}
              />
            </React.Fragment>
          ))}
        </Menu.Group>
      </Menu>
    </>
  )
}

const user = {
  id: 'U.001',
  name: 'Anna Beispiel',
  profileName: 'Anna Beispiel (persönlich)',
  shortName: 'AB'
}
const delegations = [
  {
    id: 'D.001',
    profileName: 'Diplomausstellung 2022',
    shortName: 'D2'
  },
  {
    id: 'D.002',
    profileName: 'Filmprojektgruppe Zoo',
    shortName: 'FZ'
  },
  {
    id: 'D.003',
    profileName: 'Eine superduper Ausstellung über lange Titel - Raum 302 - 2.8.-31.8.2022',
    shortName: 'EsA'
  },
  {
    id: 'D.004',
    profileName: 'mini 3',
    shortName: 'm3'
  }
]
