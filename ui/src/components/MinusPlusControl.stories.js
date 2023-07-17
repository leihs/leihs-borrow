import React, { useState } from 'react'
import MinusPlusControl from './MinusPlusControl'

export default {
  title: 'Design Components/Form Controls/MinusPlusControl',
  component: MinusPlusControl
}

export const minusPlusControl = () => {
  const [value, setValue] = useState('1')

  function change(number) {
    setValue(number)
  }

  return (
    <div className="was-validated">
      <h1>MinusPlusControl</h1>
      <label htmlFor="quantity" className="form-label">
        Quantity (1 to 10)
      </label>
      <MinusPlusControl value={value} onChange={change} required={true} min={1} max={10} id="quantity" />
      {stateDebugging(value, setValue)}
    </div>
  )
}
minusPlusControl.storyName = 'MinusPlusControl'

const DummyInput = React.forwardRef((props, ref) => (
  <div style={{ border: '1px dashed blue' }}>
    <input ref={ref} {...props} data-dummy-input />
  </div>
))

export function otherProps() {
  return (
    <div className="was-validated">
      <h1>MinusPlusControl</h1>
      <p className="text-muted">
        The attributes of the input field can be set using restProps (useful e.g. to set name or id)
      </p>
      <MinusPlusControl max={5} placeholder="Zahl" name="quantity" id="quantity" />
      <p></p>
      <p className="text-muted">
        <code>inputComponent</code>: replace the native `input` component by a modified one if needed (e.g. for Reagent)
      </p>
      <MinusPlusControl inputComponent={DummyInput} />
    </div>
  )
}

function stateDebugging(number, setNumber) {
  return (
    <div className="pt-4 text-muted">
      <h2>State debugging</h2>
      <p>number = `{number}`</p>
      <p>Simulate outside-triggered change:</p>
      <div className="d-flex gap-1">
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setNumber(undefined)}>
          undefined
        </button>
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setNumber('abc')}>
          abc
        </button>
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setNumber(-1)}>
          -1
        </button>
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setNumber(0)}>
          0
        </button>
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setNumber(1)}>
          1
        </button>
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setNumber(2)}>
          2
        </button>
        <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setNumber(11)}>
          11
        </button>
      </div>
    </div>
  )
}
