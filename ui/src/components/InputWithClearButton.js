import React, { useRef } from 'react'
import cx from 'classnames'
import { CrossIcon } from './Icons'
import PropTypes from 'prop-types'

export default function InputWithClearButton({
  className,
  onChange,
  onClear,
  value,
  inputComponent: InputComponent = 'input',
  ...restProps
}) {
  const inputRef = useRef()
  function clearClick(e) {
    inputRef.current.value = ''
    if (onClear) {
      onClear()
    } else if (onChange) {
      onChange({ ...e, target: inputRef.current })
    }
  }
  function buttonMouseDown(e) {
    e.preventDefault() // (so the button does not get focus)
  }
  return (
    <div className="position-relative">
      <InputComponent
        ref={inputRef}
        type="text"
        className={cx('form-control selectish-feedback-icon', className)}
        onChange={onChange}
        value={value}
        {...restProps}
      />
      {!!value && (
        <button
          className="btn position-absolute border-0"
          type="button"
          aria-label="Clear input"
          onClick={clearClick}
          onMouseDown={buttonMouseDown}
          tabIndex="-1"
          style={{ top: 0, right: 0 }}
        >
          <CrossIcon />
        </button>
      )}
    </div>
  )
}

InputWithClearButton.propTypes = {
  className: PropTypes.string,
  value: PropTypes.string,
  onChange: PropTypes.func,
  /** When given will be fired instead of `onChange` when the clear button is clicked */
  onClear: PropTypes.func,
  /** component to use instead of the native 'input' component if needed (e.g. for Reagent) */
  inputComponent: PropTypes.oneOfType([PropTypes.object, PropTypes.func, PropTypes.string])
}
