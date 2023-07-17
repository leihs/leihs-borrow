import React, { useState } from 'react'
import { linkTo } from '@storybook/addon-links'
import Spinner from '../components/Spinner'

export default {
  title: 'Theme/Buttons'
}

export const buttons = () => {
  const [isSpinning, setIsSpinning] = useState(false)

  return (
    <>
      <h1>Buttons</h1>
      <p className="text-muted">Primary button</p>
      <p>
        <button type="submit" className="btn btn-primary">
          Primary
        </button>
      </p>
      <p className="text-muted">Secondary button</p>
      <p>
        <button type="submit" className="btn btn-secondary">
          Secondary
        </button>
      </p>
      <p className="text-muted">Button with wait spinner</p>
      <p>
        <button
          type="submit"
          className="btn btn-secondary"
          disabled={isSpinning}
          onClick={() => {
            setIsSpinning(true)
            setTimeout(() => setIsSpinning(false), 1000)
          }}
        >
          {isSpinning ? (
            <>
              <Spinner /> Wait...
            </>
          ) : (
            'Click me'
          )}
        </button>
      </p>
      <p className="text-muted">See also: Design components to arrange buttons:</p>
      <p className="text-muted">
        <button className="btn btn-light btn-sm" onClick={linkTo('Design Components/Content/ActionButtonGroup')}>
          ActionButtonGroup
        </button>{' '}
        <button className="btn btn-light btn-sm" onClick={linkTo('Design Components/Layout/ModalDialog')}>
          ModalDialog
        </button>
      </p>
    </>
  )
}
