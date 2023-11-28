import React, { useState } from 'react'
import { linkTo } from '@storybook/addon-links'
import PageLayout from './PageLayout'
import ErrorView from './ErrorView'
import Topnav from './Topnav'
import Menu from './Menu'

export default {
  title: 'Design Components/Layout/PageLayout',
  component: PageLayout,
  parameters: {
    layout: 'fullscreen'
  }
}
const lorem =
  'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.'

export const zones = () => {
  const [navOverlayShown, setNavOverlayShown] = useState(false)

  return (
    <PageLayout
      topBar={
        <div
          className="pt-3"
          style={{ height: '50px', borderWidth: '1px', borderColor: 'blue', borderStyle: 'dashed' }}
        >
          Top row
          <label className="ms-2">
            <input type="checkbox" checked={navOverlayShown} onChange={x => setNavOverlayShown(x.target.checked)} />{' '}
            Show nav overlay <span className="not-for-burger-mode">(reduce window width to show!)</span>
          </label>
        </div>
      }
      navOverlay={<div className="bg-light-gray p-2 h-100">Nav 1</div>}
      navOverlayShown={navOverlayShown}
    >
      <div style={{ border: '1px green dashed' }}>
        <PageLayout.ContentContainer>
          <PageLayout.Header title="PageLayout - Zones" />
          <p className="text-muted">Layout zones:</p>
          <ul className="text-muted">
            <li>
              <b>Top row</b> (<span style={{ color: 'blue' }}>blue lines</span>)
            </li>
            <li>
              <b>Main row</b> (<span style={{ color: 'green' }}>green lines</span>)
              <ul>
                <li>
                  <b>Content</b>
                </li>
              </ul>
            </li>
            <li>
              <b>Nav Overlay</b> (only below "burger breakpoint")
            </li>
          </ul>
          <p className="text-muted">Views can arrange the content using the container and header components:</p>
        </PageLayout.ContentContainer>
        <PageLayout.ContentContainer style={{ border: '1px dotted #976B34', marginBottom: '1rem' }}>
          <code>PageLayout.ContentContainer</code>
          <PageLayout.Header style={{ backgroundColor: '#F1E4C9' }}>
            <code>PageLayout.Header</code>
          </PageLayout.Header>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Illum repudiandae perferendis, sed vero inventore
          ullam esse voluptates adipisci laudantium ipsa, veritatis assumenda fuga sunt, fugit impedit deserunt
          blanditiis doloribus accusamus!
        </PageLayout.ContentContainer>
      </div>
    </PageLayout>
  )
}

export const realisticExample = () => {
  const [navOverlayShown, setNavOverlayShown] = useState(false)

  function onMenuItemClick(e) {
    setNavOverlayShown(x => !x)
  }
  return (
    <PageLayout
      topBar={
        <Topnav
          cartItemCount={3}
          mainMenuLinkProps={{ onClick: onMenuItemClick }}
          mainMenuIsOpen={navOverlayShown}
          appMenuData={{ children: 'Bereiche...' }}
          userProfileShort="AB"
          desktopUserMenuData={{ children: 'Benutzermenu...' }}
        />
      }
      navOverlay={
        <Menu>
          <Menu.Group title="Group 1">
            <Menu.Link>Link 1</Menu.Link>
            <Menu.Link>Link 2</Menu.Link>
            <Menu.Link>Link 3</Menu.Link>
          </Menu.Group>
          <Menu.Group title="Group 2">
            <Menu.Link>Link 4</Menu.Link>
            <Menu.Link>Link 5</Menu.Link>
            <Menu.Link>Link 6</Menu.Link>
            <Menu.Link>Link 7</Menu.Link>
            <Menu.Link>Link 8</Menu.Link>
          </Menu.Group>
        </Menu>
      }
      navOverlayShown={navOverlayShown}
    >
      <PageLayout.ContentContainer>
        <PageLayout.Header title="PageLayout - Realistic Example" />
        <p>{lorem}</p>
        <p>{lorem}</p>
        <p>{lorem}</p>
        <p>{lorem}</p>
        <p>{lorem}</p>
      </PageLayout.ContentContainer>
    </PageLayout>
  )
}

export const horizontalInset = () => {
  return (
    <PageLayout topBar={<div className="py-3">Top bar</div>}>
      <PageLayout.ContentContainer>
        <PageLayout.Header title="PageLayout - Horizontal Inset" />
        <p className="text-muted">Note: available only below `md` breakpoint</p>
        <p className="text-muted">The content area of the page layout has a horizontal inset (padding).</p>
        <p className="text-muted">
          If for some reason edge-to-edge content is needed, use the <code>.page-inset-x-inverse</code> class.
        </p>
        <p className="text-muted">
          To re-apply horizontal inset, use <code>.page-inset-x</code>
        </p>

        <div className="border mb-3">Normal content</div>
        <div className="border mb-3 page-inset-x-inverse">Edge-to-edge content</div>
        <div className="border mb-3 page-inset-x-inverse page-inset-x">Edge-to-edge content, inset re-applied</div>
      </PageLayout.ContentContainer>
    </PageLayout>
  )
}

export const errorBoundary = () => {
  return (
    <PageLayout topBar={<div className="py-3">Top bar</div>}>
      <PageLayout.ContentContainer>
        <PageLayout.Header title="PageLayout - Error Boundaries" />
        <p className="text-muted">
          When a rendering crash happens, the error will be bounded to the zone where it happened (top bar, nav or
          content).
        </p>
        <div className="shadow mb-4"></div>
        <ErrorView
          title="Error displaying this content"
          details={'- bar\n- baz'}
          actions={[
            { title: 'Reload current page', onClick: () => document.location.reload(), variant: 'button' },
            { title: 'Go to start page', href: '/', variant: 'link-button' }
          ]}
        />
        <p className="text-muted">
          For an interactive example check out the &quot;Errors&quot; story (click on &quot;Crash this component&quot;):
        </p>
        <p className="text-muted">
          <button className="btn btn-light btn-sm" onClick={linkTo('Prototypes/Errors')}>
            Prototypes &gt; Errors
          </button>
        </p>
      </PageLayout.ContentContainer>
    </PageLayout>
  )
}
errorBoundary.storyName = 'ErrorBoundary'

export const customContentLayout = () => {
  return (
    <PageLayout topBar={<div className="py-3">Top bar</div>}>
      <div className="row">
        <div className="col-lg-3 d-none d-lg-block">
          <PageLayout.ContentContainer>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Ullam expedita ea minus? Corporis quos explicabo
            perspiciatis, sapiente eligendi consequuntur recusandae doloremque quidem, nostrum consequatur non minima
            voluptate rerum commodi possimus!
          </PageLayout.ContentContainer>
        </div>
        <div className="col-lg-9">
          <PageLayout.ContentContainer>
            <PageLayout.Header title="Custom Content Layout" />
            <p className="text-muted">This example shows a responsive 2-column layout</p>
          </PageLayout.ContentContainer>
        </div>
      </div>
    </PageLayout>
  )
}
