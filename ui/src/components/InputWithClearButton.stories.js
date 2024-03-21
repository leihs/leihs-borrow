import React, { useState } from 'react'
import InputWithClearButton from './InputWithClearButton'

export default {
  title: 'Design Components/Form Controls/InputWithClearButton',
  component: InputWithClearButton
}

export const inputWithClearButton = () => {
  const [name, setName] = useState('')

  return (
    <div>
      <h1>InputWithClearButton</h1>
      <p className="text-muted">
        An {`<input type="text" />`} with an integrated clear button. Like an {`<input type="search" />`} but with fixed
        design (not browser dependent)
      </p>
      <p className="text-muted">Controlled input example (state-bound):</p>
      <div className="d-grid gap-3">
        <InputWithClearButton
          type="text"
          name="name"
          placeholder="Name"
          value={name}
          onChange={e => setName(e.target.value)}
        />
        <input
          type="text"
          name="mirror"
          placeholder="Input bound to same state"
          className="form-control"
          value={name}
          onChange={e => setName(e.target.value)}
        />
      </div>
    </div>
  )
}
inputWithClearButton.storyName = 'InputWithClearButton'

export const inlineBlock = () => {
  const [firstName, setFirstName] = useState('James')
  const [lastName, setLastName] = useState('Bond')

  return (
    <div>
      <h1>InputWithClearButton</h1>
      <p className="text-muted">The component is based on Bootstrap&apos;s input-group and form-control classes.</p>
      <p className="text-muted">
        This means it is a block element. To place it inline, wrap it for instance in a d-inline-block.
      </p>
      <div className="d-inline-block mb-3">
        <InputWithClearButton
          name="firstName"
          placeholder="First name"
          value={firstName}
          onChange={e => setFirstName(e.target.value)}
        />
      </div>
      <div className="d-inline-block mb-3">
        <InputWithClearButton
          name="lastName"
          placeholder="Last name"
          value={lastName}
          onChange={e => setLastName(e.target.value)}
        />
      </div>
      <div className="p-2">Name = {`${firstName} ${lastName}`}</div>
    </div>
  )
}

const DummyInput = React.forwardRef((props, ref) => (
  <div style={{ border: '1px dashed blue' }}>
    <input ref={ref} {...props} data-dummy-input />
  </div>
))
DummyInput.displayName = 'DummyInput'

export const otherProps = () => {
  const [name, setName] = useState('')

  return (
    <div>
      <h1>InputWithClearButton</h1>
      <p className="text-muted">
        The attributes of the input field can be set using restProps (useful e.g. to set name, id, placeholder,
        className etc)
      </p>
      <InputWithClearButton
        type="text"
        name="name"
        placeholder="Name"
        value={name}
        onChange={e => setName(e.target.value)}
        inputComponent={DummyInput}
      />
      <p className="text-muted">
        <code>inputComponent</code>: replace the native `input` component by a modified one if needed (e.g. for Reagent)
      </p>
      <InputWithClearButton
        type="text"
        name="name"
        placeholder="Name"
        value={name}
        onChange={e => setName(e.target.value)}
        inputComponent={DummyInput}
      />
    </div>
  )
}
