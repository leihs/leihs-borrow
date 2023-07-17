import React from 'react'
import { action } from '@storybook/addon-actions'
import DownloadLink from './DownloadLink'
import Stack from './Stack'

export default {
  title: 'Design Components/Content/DownloadLink',
  component: DownloadLink
}

export const downloadLink = ({ onClick }) => {
  function handleClick(e) {
    e.preventDefault()
    onClick(e)
  }
  return (
    <div>
      <h1>DownloadLink</h1>
      <p className="text-muted">A link with a download icon:</p>
      <div className="mb-3">
        <DownloadLink onClick={handleClick} href="some/route">
          Download the specification
        </DownloadLink>
      </div>
      <p className="text-muted">To ensure spacing between multiple of them</p>
      <ul className="text-muted">
        <li>
          ...wrap a <code>{'<Stack space=3>'}</code> around all
        </li>
        <li>
          ...or a <code>{'<p>'}</code> around each
        </li>
      </ul>
      <Stack space="3">
        <DownloadLink onClick={handleClick} href="some/route">
          QX1204USB_Q1204USB_QSG.pdf
        </DownloadLink>
        <DownloadLink onClick={handleClick} href="some/route">
          QX1204USB_Q1204USB_Manual.pdf
        </DownloadLink>
        <DownloadLink onClick={handleClick} href="some/route">
          QX1204USB_Q1204USB_Specs.pdf
        </DownloadLink>
      </Stack>
    </div>
  )
}
downloadLink.storyName = 'DownloadLink'

downloadLink.args = {
  onClick: action('click')
}

export const wrap = ({ onClick }) => {
  function handleClick(e) {
    e.preventDefault()
    onClick(e)
  }
  return (
    <div>
      <h1>DownloadLink</h1>
      <p className="text-muted">Indented wrapping when the link text is too large:</p>
      <div className="p-3 shadow" style={{ width: '14rem' }}>
        <Stack space="3">
          <DownloadLink onClick={handleClick} href="some/route">
            <span className="text-break">QX1204USB_Q1204USB_QSG.pdf</span>
          </DownloadLink>
          <DownloadLink onClick={handleClick} href="some/route">
            <span className="text-break">Download the specification</span>
          </DownloadLink>
        </Stack>
      </div>
    </div>
  )
}

export const restProps = ({ onClick }) => {
  function handleClick(e) {
    e.preventDefault()
    onClick(e)
  }
  return (
    <div>
      <h1>DownloadLink</h1>
      <p className="text-muted">Set arbitrary attributes with restProps</p>
      <DownloadLink onClick={handleClick} href="some/route" className="text-primary">
        Download the Specification
      </DownloadLink>
    </div>
  )
}
