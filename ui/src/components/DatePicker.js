import React, { useEffect, useRef, useState } from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import { format, isAfter, isBefore, isSameDay, isValid, parse } from 'date-fns'
import { Calendar } from '@leihs/calendar'
import LabelInside from './LabelInside'

export default function DatePicker({
  locale,
  value,
  onChange,
  required,
  className,
  label,
  minDate,
  maxDate,
  inputComponent: InputComponent = 'input',
  ...restProps
}) {
  const formatDate = date => format(date, 'P', { locale: locale })
  const parseDate = s => parse(s, 'P', new Date(), { locale: locale })

  const containerRef = useRef()
  const inputRef = useRef()

  const [calendarActive, setCalendarActive] = useState(false)
  const [wasValidated, setWasValidated] = useState(false)
  const [validDate, setValidDate] = useState(tryParseDate(value))
  const [isInvalid, setIsInvalid] = useState()

  function tryParseDate(stringValue) {
    if (!stringValue) return null
    const d = parseDate(stringValue)
    return isValid(d) ? d : null
  }

  function handleFocus() {
    setCalendarActive(true)
  }
  function handleBlur() {
    setWasValidated(true)
  }

  function handleChange(e) {
    setValidDate(tryParseDate(e.target.value))
    updateValidationState(e.target.value)
    onChange(e)
  }

  function handleCalendarDateChange(d) {
    setValidDate(d)
    updateValidationState(formatDate(d))
    const stubEvent = { target: { value: formatDate(d) } }
    onChange(stubEvent)
  }

  function handleClickOutside(e) {
    if (containerRef.current && !containerRef.current.contains(e.target)) {
      setCalendarActive(false)
    }
  }

  // Propagate outside-triggered value change to calendar
  useEffect(() => {
    const validDateFromProp = tryParseDate(value)
    if (!isSameDay(validDateFromProp, validDate)) {
      setValidDate(tryParseDate(value))
      updateValidationState(value)
    }
  }, [value])

  // Hide calendar on outside-click
  useEffect(() => {
    if (document) {
      document.addEventListener('mousedown', handleClickOutside)
      return () => {
        document.removeEventListener('mousedown', handleClickOutside)
      }
    }
  }, [])

  function updateValidationState(v) {
    function validate() {
      if (!v) {
        return required ? 'required' : ''
      }
      const date = parseDate(v)
      if (!isValid(date)) {
        return 'unparseable'
      } else if (minDate && isBefore(date, minDate) && !isSameDay(date, minDate)) {
        return 'before minDate'
      } else if (maxDate && isAfter(date, maxDate) && !isSameDay(date, maxDate)) {
        return 'after maxDate'
      }
      return ''
    }
    const msg = validate() // (Note: message is not displayed in UI)
    inputRef.current && inputRef.current.setCustomValidity(msg)
    setIsInvalid(!!msg)
    setWasValidated(true)
  }

  function openCalendarButtonClick(e) {
    setCalendarActive(active => !active)
  }
  function openCalendarButtonMouseDown(e) {
    e.preventDefault() // (so the button does not get focus)
  }

  return (
    <div
      className={cx('date-picker', {
        'was-validated': wasValidated,
        'date-picker--calendar-active': calendarActive,
        'date-picker--invalid': isInvalid
      })}
      ref={containerRef}
    >
      <div className="position-relative">
        <LabelInside>
          <InputComponent
            ref={inputRef}
            className={cx('form-control calendar-indicator', className)}
            value={value}
            onFocus={handleFocus}
            onBlur={handleBlur}
            onChange={handleChange}
            required={required}
            autoComplete="off"
            {...restProps}
          />
          {label}
        </LabelInside>
        <button
          className="btn position-absolute"
          type="button"
          title="Eingabe löschen"
          tabIndex="-1"
          onClick={openCalendarButtonClick}
          onMouseDown={openCalendarButtonMouseDown}
          style={{ top: 0, right: 0 }}
        ></button>
      </div>
      <div className="leihs-calendar rounded-bottom">
        <Calendar
          key={validDate || 1} // force update shown date if changed via text input
          displayMode="date"
          className="m-0 w-100 rounded-bottom bg-light-shade"
          locale={locale}
          showDateDisplay={false}
          date={validDate}
          shownDate={validDate}
          onChange={handleCalendarDateChange}
          minDate={minDate}
          maxDate={maxDate}
          weekdayDisplayFormat="EEEEEE"
          showMonthAndYearPickers={false}
        />
      </div>
    </div>
  )
}

DatePicker.propTypes = {
  /** Locale for formatting and parsing of date string, e.g. `locale={de}` when `import { de } from 'date-fns/locale'` */
  locale: PropTypes.object,
  /** same as `HTMLInputElement` */
  value: PropTypes.string,
  /** same as `HTMLInputElement` */
  onChange: PropTypes.func.isRequired,
  /** same as `HTMLInputElement` */
  required: PropTypes.bool,
  /** the label (shown inside the input field) */
  label: PropTypes.node,
  /** same as `HTMLInputElement` */
  className: PropTypes.string,
  /** earliest date that can be selected */
  minDate: PropTypes.instanceOf(Date),
  /** latest date that can be selected */
  maxDate: PropTypes.instanceOf(Date),
  /** component to use instead of the native 'input' component if needed (e.g. for Reagent) */
  inputComponent: PropTypes.oneOfType([PropTypes.object, PropTypes.func, PropTypes.string])
}
