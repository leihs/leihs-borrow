import React from 'react'
import PropTypes from 'prop-types'

export default function Textarea({
  value,
  onChange,
  minRows = 3,
  maxRows = 7,
  inputComponent: InputComponent = 'textarea',
  ...restProps
}) {
  const rows = Math.max(minRows, Math.min(maxRows, String(value).split('\n').length))
  return <InputComponent rows={rows} value={value} onChange={onChange} {...restProps} />
}

Textarea.propTypes = {
  value: PropTypes.string,
  onChange: PropTypes.func,
  minRows: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  maxRows: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  /** component to use instead of the native 'textarea' component if needed (e.g. for Reagent) */
  inputComponent: PropTypes.oneOfType([PropTypes.object, PropTypes.func, PropTypes.string])
}
