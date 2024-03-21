import React, { useEffect, useRef, useState } from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import { format, isValid, parse, isSameDay, startOfMonth, isFirstDayOfMonth, isBefore, startOfDay } from 'date-fns'
import { DateRange } from '@leihs/calendar'
import LabelInside from './LabelInside'

const defaultTxt = {
  from: 'Von',
  until: 'Bis',
  placeholderFrom: 'Unbestimmt',
  placeholderUntil: 'Unbestimmt'
}
export default function DateRangePicker({
  // selection:
  selectedRange,
  onChange,
  // shown date & loading state:
  shownDate = selectedRange ? selectedRange.startDate : undefined,
  onCalendarNavigate,
  maxDateLoaded,
  now,
  // date constraints:
  minDate,
  maxDate,
  disabledDates,
  disabledStartDates,
  disabledEndDates,
  // other:
  inputComponent: InputComponent = 'input',
  className,
  locale,
  txt = defaultTxt,
  dayButtonClass,
  dayContentRenderer,
  ...restProps
}) {
  const today = startOfDay(now ? now : new Date())

  const dateFormatter = date => format(date, 'P', { locale: locale })
  const dateParser = s => parse(s, 'P', new Date(), { locale: locale })

  // DateRange component

  // "Focus" - for interaction with the date calendar component (values: 'startDate' | 'endDate')
  const [focus, setFocus] = useState('startDate')

  const range = (() => {
    const rangeConfig = {
      key: 'selection',
      showDateDisplay: false // (because we show the date input fields separately)
    }
    const { startDate, endDate } = selectedRange
    return {
      ...rangeConfig,
      startDate,
      endDate
    }
  })()

  const focusedRange = focus === 'startDate' ? [0, 0] : [0, 1]

  function handleSelectionChange(item) {
    const { startDate, endDate } = item.selection
    onChange({ startDate, endDate })
  }

  function handleRangeFocusChange(range) {
    if (range[1] === 1) {
      setFocus('endDate')
    } else {
      setFocus('startDate')
    }
  }

  // satellite inputs

  const startDateInput = useRef()
  const [startDateInvalid, setStartDateInvalid] = useState(false)

  const endDateInput = useRef()
  const [endDateInvalid, setEndDateInvalid] = useState(false)

  useEffect(() => {
    startDateInput.current && (startDateInput.current.value = dateFormatter(selectedRange.startDate))
    endDateInput.current && (endDateInput.current.value = dateFormatter(selectedRange.endDate))
    setStartDateInvalid(false)
    setEndDateInvalid(false)
  }, [selectedRange])

  function handleInputBlur(e) {
    const date = dateParser(e.target.value)
    const valid = isValid(date)
    const { startDate, endDate } = selectedRange
    function handleStartDate(date) {
      if (valid) {
        if (!isSameDay(date, startDate)) {
          onChange({
            startDate: date,
            endDate: endDate >= date ? endDate : date
          })
        }
      }
      setStartDateInvalid(!valid)
    }
    function handleEndDate(date) {
      if (valid) {
        if (!isSameDay(date, endDate)) {
          onChange({
            startDate: startDate <= date ? startDate : date,
            endDate: date
          })
        }
      }
      setEndDateInvalid(!valid)
    }
    if (e.target.name === 'startDate') {
      handleStartDate(date)
    } else {
      handleEndDate(date)
    }
  }

  function handleInputFocus(e) {
    setFocus(e.target.name)
  }

  const minDateDayStart = minDate ? startOfDay(minDate) : undefined
  const minDateStartOfMonth =
    minDateDayStart && !isFirstDayOfMonth(minDateDayStart) ? startOfMonth(minDate) : minDateDayStart

  function getDayConfig(day) {
    const isPast = isBefore(day, today)
    const isBeforeMinDate = isBefore(day, minDateDayStart)
    const isDisabled = !isPast && disabledDates && disabledDates.some(d => isSameDay(day, d))
    const isDisabledStart = !isPast && disabledStartDates && disabledStartDates.some(d => isSameDay(day, d))
    const isDisabledEnd = !isPast && disabledEndDates && disabledEndDates.some(d => isSameDay(day, d))
    const customClassNames = cx(
      'cal-day',
      {
        'cal-day--past': isPast,
        'cal-day--before-min-date': isBeforeMinDate,
        'cal-day--under-availability': isDisabled,
        'cal-day--invalid-as-start': isDisabledStart,
        'cal-day--invalid-as-end': isDisabledEnd
      },
      dayButtonClass
    )
    return { customClassNames }
  }

  return (
    <div className={cx('date-range-picker ui-date-range-picker', className)} {...restProps}>
      <div className="mb-3">
        <LabelInside>
          <InputComponent
            ref={startDateInput}
            name="startDate"
            id="startDate"
            className={cx('form-control calendar-indicator', { 'is-invalid': startDateInvalid })}
            defaultValue={dateFormatter(selectedRange.startDate)}
            onBlur={handleInputBlur}
            onFocus={handleInputFocus}
            onKeyPress={e => e.key === 'Enter' && handleInputBlur(e)}
            placeholder={txt.placeholderFrom}
            autoComplete="off"
          />
          <label htmlFor="startDate">{txt.from}</label>
        </LabelInside>
      </div>
      <div className="mb-3">
        <LabelInside>
          <InputComponent
            ref={endDateInput}
            name="endDate"
            id="endDate"
            className={cx('form-control calendar-indicator', { 'is-invalid': endDateInvalid })}
            defaultValue={dateFormatter(selectedRange.endDate)}
            onBlur={handleInputBlur}
            onFocus={handleInputFocus}
            onKeyPress={e => e.key === 'Enter' && handleInputBlur(e)}
            placeholder={txt.placeholderUntil}
            autoComplete="off"
          />
          <label htmlFor="endDate">{txt.until}</label>
        </LabelInside>
      </div>
      <div className={cx('leihs-calendar')}>
        <DateRange
          key={shownDate || 1} // force update shown date if changed via text input
          // selection:
          ranges={[range]}
          onChange={handleSelectionChange}
          // shown date & loading state:
          shownDate={shownDate}
          focusedRange={focusedRange}
          onRangeFocusChange={handleRangeFocusChange}
          onShownDateChange={onCalendarNavigate}
          maxDateLoaded={maxDateLoaded}
          loadingIndicator={<div>LOADING...</div>}
          // date constraints:
          minDate={minDateStartOfMonth}
          maxDate={maxDate}
          // appearance and behaviour:
          className="m-0 w-100 rounded bg-light-shade"
          direction="vertical"
          months={1}
          weekStartsOn={1}
          weekdayDisplayFormat="EEEEEE"
          showMonthAndYearPickers={false}
          rangeColors={['rgb(150, 150, 150)']}
          editableDateInputs={false}
          locale={locale}
          fixedHeight={false}
          dayContentRenderer={dayContentRenderer}
          dayConfigGetter={getDayConfig}
        />
      </div>
    </div>
  )
}

DateRangePicker.propTypes = {
  // selection:

  /** selected date range */
  selectedRange: PropTypes.shape({
    startDate: PropTypes.instanceOf(Date).isRequired,
    endDate: PropTypes.instanceOf(Date).isRequired
  }).isRequired,
  /** callback when the selected date range is modified (argument: `selectedRange`, shaped as described above) */
  onChange: PropTypes.func.isRequired,

  // shown date & loading state:

  /** a date in the month to initially be shown, defaults to `selectedRange.startDate` */
  shownDate: PropTypes.instanceOf(Date),
  /** callback when the shown month is changed using the calendar navigation (argument: a date in the now-shown month) */
  onCalendarNavigate: PropTypes.func,
  /** Date until which the underlying data was loaded (availability, holidays etc).
   * When given, calendar shows a "loading" overlay for any month after this date. The controlling component
   * must subscribe to `onCalendarNavigate` to update this date, and also to `onChange` (to handle start date
   * modification via keyboard). */
  maxDateLoaded: PropTypes.instanceOf(Date),
  /** Now (defaults to `new Date()`) */
  now: PropTypes.instanceOf(Date),

  // date constraints:

  /** lower limit of the viewable calendar dates */
  minDate: PropTypes.instanceOf(Date),
  /** upper limit of the viewable calendar dates */
  maxDate: PropTypes.instanceOf(Date),
  /** dates which are unselectable (must not be within the selected range) */
  disabledDates: PropTypes.arrayOf(PropTypes.instanceOf(Date)),
  /** dates which are unselectable as begin date */
  disabledStartDates: PropTypes.arrayOf(PropTypes.instanceOf(Date)),
  /** dates wich are unselectable as end date */
  disabledEndDates: PropTypes.arrayOf(PropTypes.instanceOf(Date)),

  // other:

  /** the date-fns locale, e.g. `import { de } from 'date-fns/locale'` */
  dateLocale: PropTypes.object,
  /** component to use instead of the native 'input' component if needed (e.g. for Reagent) */
  inputComponent: PropTypes.oneOfType([PropTypes.object, PropTypes.func, PropTypes.string]),
  /** class applied to all day buttons */
  dayButtonClass: PropTypes.string,
  /** Overrides the content of the day buttons, e.g. `day => <span>{format(day, 'd')</span>` */
  dayContentRenderer: PropTypes.func,
  /** custom CSS class(es) to add to the component */
  className: PropTypes.string,
  /** `date-fns` locale */
  locale: PropTypes.object,
  /** Text dictionary */
  txt: PropTypes.object
}
