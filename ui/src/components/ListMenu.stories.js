import React from 'react'
import ListMenu from './ListMenu'

export default {
  title: 'Design Components/Layout/ListMenu',
  component: ListMenu
}

function clickHandler(e) {
  e.preventDefault()
  alert('click')
}

export function listMenu() {
  return (
    <>
      <h1>ListMenu</h1>
      <p className="text-muted">A compact simple menu intended as side nav</p>
      <div className="mb-4">
        <ListMenu>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Mikrophone
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route" isSelected>
            Lautsprecher
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Analoge Peripheriegeräte
          </ListMenu.Link>
        </ListMenu>
      </div>
      <p className="text-muted">Edge cases:</p>
      <div style={{ maxWidth: '300px' }} className="mb-4">
        <ListMenu>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Long Phrase: Voluptatibus nobis sed id modi dolor unde veritatis? Provident?
          </ListMenu.Link>{' '}
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            LongWordVoluptatibusnobissedidmodidolorundeveritatis?Provident?
          </ListMenu.Link>
          <ListMenu.Button onClick={clickHandler}>This is a button (not a link)</ListMenu.Button>
        </ListMenu>
      </div>
    </>
  )
}

export function menuWithBreadcrumbs() {
  return (
    <>
      <h1>ListMenu</h1>
      <p className="text-muted">Menu with breadcrumbs</p>
      <p className="text-muted">Level 1 (dash indicates nested categories):</p>
      <div className="ms-4 mb-4">
        <ListMenu>
          <ListMenu.Link isBreadcrumb onClick={clickHandler} href="#some/route">
            Alle Kategorien
          </ListMenu.Link>
          <ListMenu.CurrentItem>Fotografie</ListMenu.CurrentItem>
          <ListMenu.Link onClick={clickHandler} href="#some/route" hasChildren>
            Fotografie analog
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route" hasChildren>
            Fotografie digital
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Blitz/Licht/Studio
          </ListMenu.Link>
        </ListMenu>
      </div>

      <p className="text-muted">Level 2:</p>
      <div className="ms-4 mb-4">
        <ListMenu>
          <ListMenu.Link isBreadcrumb onClick={clickHandler} href="#some/route">
            Alle Kategorien
          </ListMenu.Link>
          <ListMenu.Link isBreadcrumb onClick={clickHandler} href="#some/route">
            Fotografie
          </ListMenu.Link>
          <ListMenu.CurrentItem>Fotografie digital</ListMenu.CurrentItem>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Digitale Mittelformatkameras
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Digitale Spiegelreflexkameras
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Kompaktkameras
          </ListMenu.Link>
        </ListMenu>
      </div>

      <p className="text-muted">Level 3 (without children; siblings are shown):</p>
      <div className="ms-4 mb-4">
        <ListMenu>
          <ListMenu.Link isBreadcrumb onClick={clickHandler} href="#some/route">
            Alle Kategorien
          </ListMenu.Link>
          <ListMenu.Link isBreadcrumb onClick={clickHandler} href="#some/route">
            Fotografie
          </ListMenu.Link>
          <ListMenu.Link
            isBreadcrumb
            onClick={clickHandler}
            href="#some/route"
            isSelected
            style={{ marginBottom: '12px' }}
          >
            Fotografie digital
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Digitale Mittelformatkameras
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} isSelected href="#some/route">
            Digitale Spiegelreflexkameras
          </ListMenu.Link>
          <ListMenu.Link onClick={clickHandler} href="#some/route">
            Kompaktkameras
          </ListMenu.Link>
        </ListMenu>
      </div>
    </>
  )
}
