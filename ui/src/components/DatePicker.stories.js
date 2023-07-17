import React, { useState } from 'react'
import { de } from 'date-fns/locale'

import DatePicker from './DatePicker'

export default {
  title: 'Design Components/Form Controls/DatePicker',
  component: DatePicker
}

export const datePicker = () => {
  const [value, setValue] = useState('01.08.2021')
  return (
    <div>
      <h1>DatePicker</h1>
      <DatePicker
        locale={de}
        value={value}
        onChange={e => setValue(e.target.value)}
        id="the-date"
        label={<label htmlFor="the-date">Date</label>}
      />
      <div className="text-muted pt-5">
        <label htmlFor="app-state" className="form-label">
          {'Input to simulate "app state" for controlled input:'}
        </label>
        <input
          type="text"
          id="app-state"
          value={value}
          onChange={e => setValue(e.target.value)}
          className="form-control form-control-sm"
        />
      </div>
    </div>
  )
}
datePicker.storyName = 'DatePicker'

export const invalidInput = () => {
  const [value, setValue] = useState("ceci n'est pas une date")
  return (
    <div className="was-validated">
      <h1>DatePicker</h1>
      <DatePicker
        locale={de}
        value={value}
        onChange={e => setValue(e.target.value)}
        id="the-date"
        label={<label htmlFor="the-date">Date</label>}
      />
    </div>
  )
}

export const required = () => {
  const [value, setValue] = useState('')
  return (
    <div className="was-validated">
      <h1>DatePicker</h1>
      <DatePicker
        locale={de}
        value={value}
        onChange={e => setValue(e.target.value)}
        required
        id="the-date"
        label={<label htmlFor="the-date">Date</label>}
      />
    </div>
  )
}

export const constraints = () => {
  const [value, setValue] = useState('01.08.2021')
  return (
    <div className="was-validated">
      <h1>DatePicker</h1>
      <p className="text-muted">Only dates from 16 to 22 selectable</p>
      <DatePicker
        locale={de}
        value={value}
        onChange={e => setValue(e.target.value)}
        id="the-date"
        label={<label htmlFor="the-date">Date</label>}
        minDate={new Date('2021-08-16')}
        maxDate={new Date('2021-08-22')}
      />
    </div>
  )
}

const DummyInput = React.forwardRef((props, ref) => (
  <div style={{ border: '1px dashed blue' }}>
    <input ref={ref} {...props} data-dummy-input />
  </div>
))

export const otherProps = () => {
  const [value, setValue] = useState('01.08.2021')

  return (
    <div className="was-validated">
      <h1>DatePicker</h1>
      <p className="text-muted">
        The attributes of the input field can be set using restProps (useful e.g. to set name, id, placeholder,
        className etc)
      </p>
      <DatePicker
        locale={de}
        value={value}
        label={<label htmlFor="the-date">Date</label>}
        onChange={e => setValue(e.target.value)}
        name="date-of-birth"
        className="bg-info"
      />
      <p></p>
      <p className="text-muted">
        <code>inputComponent</code>: replace the native `input` component by a modified one if needed (e.g. for Reagent)
      </p>
      <DatePicker
        locale={de}
        value={value}
        label={<label htmlFor="the-date">Date</label>}
        onChange={e => setValue(e.target.value)}
        inputComponent={DummyInput}
      />
    </div>
  )
}
