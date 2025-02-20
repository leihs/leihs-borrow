import React from 'react'
import Menu from './Menu'

export default {
  title: 'Design Components/Navigation/Menu',
  component: Menu
}

function clickHandler(e) {
  e.preventDefault()
  alert('click')
}

export function menu() {
  return (
    <>
      <h1>Menu</h1>
      <p className="text-muted">Presents a grouped list of links or buttons</p>
      <div className="position-relative">
        <Menu>
          <Menu.Group title="Group 1">
            <Menu.Link onClick={clickHandler} href="#some/route">
              Link 1
            </Menu.Link>
            <Menu.Link onClick={clickHandler} href="#some/route">
              Link 2
            </Menu.Link>
            <Menu.Link onClick={clickHandler} href="#some/route">
              Link 3
            </Menu.Link>
          </Menu.Group>
          <Menu.Group title="Group 2">
            <Menu.Button onClick={clickHandler}>Button 1</Menu.Button>
            <Menu.Button onClick={clickHandler}>Button 2</Menu.Button>
          </Menu.Group>
        </Menu>
      </div>
    </>
  )
}

export function group() {
  return (
    <>
      <h1>Menu.Group</h1>
      <p className="text-muted">Layout container for a set of links or buttons</p>
      <Menu.Group>
        <Menu.Link onClick={clickHandler}>Lion</Menu.Link>
        <Menu.Link onClick={clickHandler}>Tiger</Menu.Link>
      </Menu.Group>
      <p></p>
      <p className="text-muted">Optionally with a title:</p>
      <Menu.Group title="Animals">
        <Menu.Link onClick={clickHandler}>Lion</Menu.Link>
        <Menu.Link onClick={clickHandler}>Tiger</Menu.Link>
      </Menu.Group>
      <p></p>
    </>
  )
}

export function horizontalGroup() {
  return (
    <>
      <h1>Menu.HorizontalGroup</h1>
      <Menu.HorizontalGroup title="Animals">
        <Menu.Link onClick={clickHandler}>Lion</Menu.Link>
        <Menu.Link onClick={clickHandler}>Tiger</Menu.Link>
      </Menu.HorizontalGroup>
      <p></p>
      <p className="text-muted">Wraps when there are too many items for one line</p>
      <Menu.HorizontalGroup title="Lorem" style={{ maxWidth: '360px' }} className="p-4 border">
        <Menu.Link onClick={clickHandler}>ipsum</Menu.Link>
        <Menu.Link onClick={clickHandler}>dolor</Menu.Link>
        <Menu.Link onClick={clickHandler}>sit</Menu.Link>
        <Menu.Link onClick={clickHandler}>amet</Menu.Link>
        <Menu.Link onClick={clickHandler}>consectetur</Menu.Link>
      </Menu.HorizontalGroup>
    </>
  )
}

export function selectedItem() {
  return (
    <Menu>
      <h1>Selected item</h1>
      <Menu.Group title="Animals">
        <Menu.Link onClick={clickHandler}>Lion</Menu.Link>
        <Menu.Link onClick={clickHandler}>Tiger</Menu.Link>
        <Menu.Link onClick={clickHandler} isSelected>
          Floppa
        </Menu.Link>
      </Menu.Group>
      <Menu.HorizontalGroup title="Horizontal">
        <Menu.Link onClick={clickHandler}>Lion</Menu.Link>
        <Menu.Link onClick={clickHandler}>Tiger</Menu.Link>
        <Menu.Link onClick={clickHandler} isSelected>
          Floppa
        </Menu.Link>
      </Menu.HorizontalGroup>
    </Menu>
  )
}
