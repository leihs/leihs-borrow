import React, { useState } from 'react'

import Topnav from '../components/Topnav'
import Menu from '../components/Menu'
import PageLayout from '../components/PageLayout'
import ListCard from '../components/ListCard'
import ActionButtonGroup from '../components/ActionButtonGroup'

export default {
  title: 'Prototypes/Layout And Navigation',
  parameters: { layout: 'fullscreen' }
}

export function layoutAndNavigation() {
  const [overlay, setOverlay] = useState('') // '' | 'main' | 'user' | 'app'
  function onMainMenubuttonClick() {
    setOverlay(x => (x === 'main' ? '' : 'main'))
  }
  function onUserMenuButtonClick() {
    setOverlay(x => (x === 'user' ? '' : 'user'))
  }
  function onAppButtonClick() {
    setOverlay(x => (x === 'app' ? '' : 'app'))
  }

  const dismissOverlay = () => {
    setOverlay('')
  }

  return (
    <PageLayout
      topBar={
        <Topnav
          brandName="Leihs"
          brandLinkProps={{ role: 'button', onClick: dismissOverlay }}
          mainMenuIsOpen={overlay === 'main'}
          mainMenuLinkProps={{ onClick: onMainMenubuttonClick, 'aria-controls': 'menu' }}
          mainMenuItems={[
            { href: '#', label: 'Katalog', selected: true },
            { href: '#', label: 'Bestellungen' },
            { href: '#', label: 'Favoriten' },
            { href: '#', label: 'Inventarparks' },
            { href: '#', label: 'Hilfe' }
          ]}
          cartItemCount={0}
          cartItemLinkProps={{ onClick: dismissOverlay }}
          userMenuIsOpen={overlay === 'user'}
          userProfileShort="AB"
          userMenuLinkProps={{
            onClick: onUserMenuButtonClick,
            'aria-controls': 'user-menu'
          }}
          appMenuIsOpen={overlay === 'app'}
          appMenuLinkLabel="Ausleihen"
          appMenuLinkProps={{
            onClick: onAppButtonClick,
            'aria-controls': 'app-menu'
          }}
        />
      }
      nav1={
        <Menu id="menu">
          <Menu.Group title="Ausleihen">
            <Menu.Link onClick={dismissOverlay}>Katalog</Menu.Link>
            <Menu.Link onClick={dismissOverlay}>Warenkorb</Menu.Link>
            <Menu.Link onClick={dismissOverlay}>Bestellungen</Menu.Link>
            <Menu.Link onClick={dismissOverlay}>Favoriten</Menu.Link>
            <Menu.Link onClick={dismissOverlay}>Inventarparks</Menu.Link>
            <Menu.Link onClick={dismissOverlay}>Hilfe</Menu.Link>
          </Menu.Group>
          <Menu.Group title="Weitere Bereiche">
            <Menu.Link onClick={dismissOverlay}>Leihs Admin</Menu.Link>
            <Menu.Link onClick={dismissOverlay}>Bedarfsermittlung</Menu.Link>
            <Menu.Link onClick={dismissOverlay}>Ausleihe Toni-Areal</Menu.Link>
          </Menu.Group>
        </Menu>
      }
      nav1Shown={overlay === 'main'}
      nav2={
        overlay === 'user' ? (
          <Menu id="user-menu">
            <Menu.Group title="Anna Beispiel">
              <Menu.Link href="">Benutzerdaten</Menu.Link>
              <Menu.Link href="">Abmelden</Menu.Link>
            </Menu.Group>
            <Menu.Group title="Sprache">
              <Menu.Link href="" isSelected>
                Deutsch
              </Menu.Link>
              <Menu.Link href="">English</Menu.Link>
              <Menu.Link href="">Français</Menu.Link>
            </Menu.Group>
          </Menu>
        ) : (
          <Menu id="app-menu">
            <Menu.Group title="Bereich">
              <Menu.Link onClick={dismissOverlay} isSelected>
                Ausleihen
              </Menu.Link>
              <Menu.Link onClick={dismissOverlay}>Leihs Admin</Menu.Link>
              <Menu.Link onClick={dismissOverlay}>Bedarfsermittlung</Menu.Link>
              <Menu.Link onClick={dismissOverlay}>Ausleihe Toni-Areal</Menu.Link>
            </Menu.Group>
          </Menu>
        )
      }
      nav2Shown={overlay === 'user' || overlay === 'app'}
      onContentClick={dismissOverlay}
    >
      <PageLayout.ContentContainer>
        <PageLayout.Header title="Page Title"></PageLayout.Header>
        <p className="text-muted d-lg-none">Click the hamburger icon to open the main menu.</p>
        <p className="text-muted d-none d-lg-block">
          Click the button &quot;Bereich&quot; to open the app switcher menu.
        </p>
        <p className="text-muted">
          Click the button with the user&apos;s initials (&quot;AB&quot;) to open the user menu.
        </p>
        <h1 className="d-sm-none">xs</h1>
        <h1 className="d-none d-sm-block d-md-none">sm</h1>
        <h1 className="d-none d-md-block d-lg-none">md</h1>
        <h1 className="d-none d-lg-block">lg, xl, xxl</h1>
        <p className="fw-bold">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Excepturi nostrum ducimus perspiciatis voluptatibus
          molestiae deserunt suscipit nobis temporibus saepe quos. Quos id amet a nam dicta distinctio unde minima
          obcaecati? Laboriosam velit in dicta dignissimos ullam, suscipit dolore? Facilis corrupti amet, facere
          placeat, inventore dolorum nemo molestias repellat consequuntur error iure architecto necessitatibus tenetur
          voluptas fugiat, totam maiores odio illo?
        </p>
        <ListCard.Stack className="mb-4">
          <ListCard>
            <ListCard.Title>List card</ListCard.Title>
          </ListCard>
        </ListCard.Stack>
        <ActionButtonGroup className="mb-4">
          <button className="btn btn-primary">Button</button>
          <button className="btn btn-secondary">Button</button>
        </ActionButtonGroup>
        <p className="fw-bold">
          Ut animi nostrum explicabo, exercitationem cupiditate quia perferendis labore ex blanditiis architecto!
          Architecto cum repellat laudantium impedit quo? Quo velit facilis qui fugiat eos dicta dolores cum fugit
          voluptatum quaerat! Unde optio voluptatem sed laboriosam dolorum doloribus itaque vel cum harum animi,
          possimus illo magni cupiditate non minus quos soluta quasi excepturi accusantium sit deleniti provident dicta
          eos quas? Voluptas? Velit voluptatum labore consectetur expedita corporis perferendis fugit non doloremque
          tempore nisi corrupti, beatae voluptates soluta, nemo omnis reiciendis pariatur magnam quasi veritatis
          sapiente eveniet! Odio neque officia at praesentium.
        </p>
      </PageLayout.ContentContainer>
    </PageLayout>
  )
}
