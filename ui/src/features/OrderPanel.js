import React, { useEffect, useState } from 'react'
import {
  startOfDay,
  addMonths,
  parseISO,
  isValid,
  isBefore,
  eachDayOfInterval,
  addDays,
  isSameDay,
  format,
  formatISO
} from 'date-fns'
import { enGB as defaultDateLocale } from 'date-fns/locale'
import { translate as t } from '../lib/translate'
import Let from '../lib/Let'
import Section from '../components/Section'
import MinusPlusControl from '../components/MinusPlusControl'
import DateRangePicker from '../components/DateRangePicker'
import Warning from '../components/Warning'
import InfoMessage from '../components/InfoMessage'
import orderPanelPropTypes from './OrderPanelPropTypes'
import cx from 'classnames'

const noop = () => {}

const OrderPanel = ({
  modelData,
  profileName,
  //
  now,
  maxDateTotal,
  maxDateLoaded,
  onCalendarNavigate = noop,
  //
  initialStartDate,
  initialEndDate,
  onDatesChange = noop,
  //
  initialQuantity = 1,
  onQuantityChange = noop,
  //
  inventoryPools,
  initialInventoryPoolId,
  onInventoryPoolChange = noop,
  //
  initialPickupLocationId,
  onPickupLocationChange = noop,
  //
  initialShowDayQuants = false,
  onShowDayQuantsChange = noop,
  //
  onValidate = noop,
  onSubmit = noop,
  locale,
  dateLocale,
  txt = {}
}) => {
  const { label } = txt
  const isTransportable = modelData.transportable !== false
  const anyPoolHasPickupLocations = inventoryPools.some(pool => pool.pickupLocations?.length > 0)

  const today = startOfDay(now ? now : new Date())
  const maxDate = maxDateTotal ? startOfDay(maxDateTotal) : addMonths(today, 20 * 12)

  const [quantity, setQuantity] = useState(initialQuantity)
  const [selectedPoolId, setSelectedPoolId] = useState(initialInventoryPoolId || inventoryPools?.[0]?.id || 'NO_POOLS')
  const [selectedPickupLocationId, setSelectedPickupLocationId] = useState(
    isTransportable ? initialPickupLocationId || null : null
  )
  const [selectedRange, setSelectedRange] = useState({
    startDate: initialStartDate ? startOfDay(initialStartDate) : today,
    endDate: initialEndDate ? startOfDay(initialEndDate) : addDays(today, 1)
  })

  // State depending on the input states (e.g. validation result)
  const [dependentState, setDependentState] = useState()
  useEffect(() => {
    // Make sure the selected pool is in list (otherwise fill-in a surrogate)
    const poolFromList = inventoryPools.find(x => x.id === selectedPoolId)
    const selectedPool = poolFromList || {
      id: selectedPoolId,
      name: t(txt.validate, 'unknown-pool', locale),
      isSurrogate: true
    }
    const selectablePools = poolFromList ? inventoryPools : [selectedPool, ...inventoryPools]
    const poolPickupLocations = selectedPool.pickupLocations || []
    const showPickupLocationSelect = anyPoolHasPickupLocations && isTransportable && poolPickupLocations.length > 0
    const resolvedPickupLocationId =
      showPickupLocationSelect && poolPickupLocations.some(loc => loc.id === selectedPickupLocationId)
        ? selectedPickupLocationId
        : null
    const initialPickupUnavailable =
      Boolean(initialPickupLocationId) &&
      (!isTransportable || !poolPickupLocations.some(loc => loc.id === initialPickupLocationId))

    // Get availability data for selected pool
    const { availability } = modelData
    const poolAvailability = (() => {
      const tmp = availability.find(x => x.inventoryPool.id === selectedPool.id)
      if (!tmp) {
        return { inventoryPool: selectedPool, dates: [] }
      }
      const rawDates = tmp.dates || []
      return {
        ...tmp,
        dates: rawDates.map(x => ({
          ...x,
          parsedDate: parseISO(x.date)
        }))
      }
    })()

    // Extract data for DateRangePicker
    const { disabledDates, disabledStartDates, disabledEndDates } = getDateRangePickerConstraints(
      poolAvailability,
      today,
      quantity
    )
    const maxQuantityByDay = getMaxQuantityByDay(poolAvailability)

    // Validation
    const validationResult = validate(selectedPool, poolAvailability, resolvedPickupLocationId)

    setDependentState({
      selectablePools,
      selectedPool,
      poolPickupLocations,
      showPickupLocationSelect,
      resolvedPickupLocationId,
      initialPickupUnavailable,
      poolAvailability,
      disabledDates,
      disabledStartDates,
      disabledEndDates,
      maxQuantityByDay,
      validationResult
    })

    onValidate(validationResult.isValid)
  }, [
    quantity,
    selectedPoolId,
    selectedPickupLocationId,
    selectedRange,
    modelData,
    maxDateTotal,
    maxDateLoaded,
    inventoryPools,
    locale,
    anyPoolHasPickupLocations,
    isTransportable,
    initialPickupLocationId
  ])

  // Validation
  function validate(selectedPool, poolAvailability, pickupLocationId) {
    const poolError = validatePool(selectedPool, locale, txt.validate)
    if (poolError) {
      return { poolError }
    }
    const dateRangeErrors = validateDateRange(
      selectedRange,
      today,
      maxDate,
      poolAvailability,
      quantity,
      locale,
      dateLocale,
      txt.validate,
      Boolean(pickupLocationId)
    )
    if (dateRangeErrors && dateRangeErrors.length > 0) {
      return { dateRangeErrors: [...dateRangeErrors] }
    }
    return { isValid: true }
  }

  const [showDayQuants, setShowDayQuants] = useState(initialShowDayQuants)

  // Event handlers

  function submit(e) {
    e.preventDefault()
    const validationResult = validate(
      dependentState.selectedPool,
      dependentState.poolAvailability,
      dependentState.resolvedPickupLocationId
    )
    if (validationResult.isValid) {
      onSubmit(stateForCallbacks())
    }
  }

  function changeQuantity(number) {
    setQuantity(number)
    onQuantityChange({ ...stateForCallbacks(), quantity: number })
  }

  function changeInventoryPool(e) {
    const id = e.target.value
    const nextPool = inventoryPools.find(x => x.id === id)
    const previousPool = inventoryPools.find(x => x.id === selectedPoolId)
    const nextPickupLocationId = resolvePickupLocationOnPoolChange({
      isTransportable,
      selectedPickupLocationId,
      previousPool,
      nextPool
    })
    setSelectedPoolId(id)
    setSelectedPickupLocationId(nextPickupLocationId)
    onInventoryPoolChange({
      ...stateForCallbacks(),
      poolId: id,
      pickupLocationId: nextPickupLocationId
    })
  }

  function changePickupLocation(e) {
    const id = e.target.value || null
    setSelectedPickupLocationId(id)
    onPickupLocationChange({ ...stateForCallbacks(), pickupLocationId: id })
  }

  function changeDateRange(range) {
    setSelectedRange(range)
    onDatesChange({ ...stateForCallbacks(), ...range })
  }

  const stateForCallbacks = () => ({
    startDate: selectedRange.startDate,
    endDate: selectedRange.endDate,
    quantity,
    poolId: selectedPoolId,
    pickupLocationId: dependentState?.resolvedPickupLocationId ?? selectedPickupLocationId
  })

  function handleCalendarNavigate(newDate) {
    onCalendarNavigate({ date: newDate })
  }

  function changeShowDayQuants(e) {
    setShowDayQuants(e.target.checked)
    onShowDayQuantsChange(e.target.checked)
  }

  if (!dependentState) {
    return null
  }
  const {
    selectablePools,
    selectedPool,
    poolPickupLocations,
    showPickupLocationSelect,
    resolvedPickupLocationId,
    initialPickupUnavailable,
    disabledDates,
    disabledStartDates,
    disabledEndDates,
    maxQuantityByDay,
    validationResult
  } = dependentState

  const mainWarehouseLabel = selectedPool.defaultPickupLocationName || t(label, 'main-warehouse', locale)

  function renderDay(day) {
    const isoDate = formatISO(day, { representation: 'date' })
    const nofAvailable = showDayQuants && day >= today ? maxQuantityByDay[isoDate] : undefined
    const showQuantity = nofAvailable !== undefined
    return (
      <>
        <span className={cx('opcal__day-num', { 'opcal__day-num--with-quantity': showQuantity })}>
          {format(day, 'd')}
        </span>
        {showQuantity && <div className="opcal__day-quantity">{nofAvailable}</div>}
      </>
    )
  }

  return (
    <form onSubmit={submit} noValidate className="was-validated" autoComplete="off" id="order-dialog-form">
      <div className="d-grid gap-4">
        {profileName && (
          <Section title={t(label, 'user-delegation', locale)}>
            <div className="fw-bold">{profileName}</div>
          </Section>
        )}
        <Section title={t(label, 'pool', locale)}>
          <label htmlFor="pool-id" className="visually-hidden">
            {t(label, 'pool', locale)}
          </label>
          <select
            name="pool-id"
            id="pool-id"
            value={selectedPoolId}
            onChange={changeInventoryPool}
            className="form-select"
          >
            {selectablePools.map(({ id, name, totalReservableQuantity }) => (
              <option key={id} value={id}>
                {totalReservableQuantity
                  ? t(label, 'pool-max-amount', locale, { pool: name, amount: totalReservableQuantity })
                  : name}
              </option>
            ))}
          </select>
          {validationResult.poolError ? (
            <Warning className="mt-2">{validationResult.poolError}</Warning>
          ) : (
            <>
              <InfoMessage className="mt-2">
                {t(label, 'pool-max-amount-info', locale, { amount: selectedPool.totalReservableQuantity })}
              </InfoMessage>
              {!isTransportable && anyPoolHasPickupLocations && (
                <InfoMessage className="mt-2">{t(label, 'not-transportable', locale)}</InfoMessage>
              )}
            </>
          )}
        </Section>

        {!validationResult.poolError && (
          <Let title={t(label, 'timespan', locale)}>
            {({ title }) => (
              <div className="d-grid gap-4">
                {showPickupLocationSelect && (
                  <Section title={t(label, 'pickup-location', locale)}>
                    <label htmlFor="pickup-location-id" className="visually-hidden">
                      {t(label, 'pickup-location', locale)}
                    </label>
                    <select
                      key={selectedPoolId}
                      name="pickup-location-id"
                      id="pickup-location-id"
                      value={resolvedPickupLocationId || ''}
                      onChange={changePickupLocation}
                      className="form-select"
                    >
                      <option value="">{mainWarehouseLabel}</option>
                      {poolPickupLocations.map(({ id, name }) => (
                        <option key={id} value={id}>
                          {name}
                        </option>
                      ))}
                    </select>
                    {initialPickupUnavailable ? (
                      <Warning className="mt-2">
                        {t(label, 'unavailable-initial-pickup-location', locale)}
                      </Warning>
                    ) : (
                      <InfoMessage className="mt-2">
                        <a
                          className="decorate-links"
                          href={`/borrow/inventory-pools/${selectedPoolId}`}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          {t(label, 'pickup-locations-more-details', locale)}
                        </a>
                      </InfoMessage>
                    )}
                  </Section>
                )}
                {initialPickupUnavailable && !showPickupLocationSelect && (
                  <Warning>{t(label, 'unavailable-initial-pickup-location', locale)}</Warning>
                )}
                <Section title={t(label, 'quantity', locale)}>
                  <label htmlFor="quantity" className="visually-hidden">
                    {t(label, 'quantity', locale)}
                  </label>
                  <MinusPlusControl name="quantity" id="quantity" value={quantity} onChange={changeQuantity} min={1} />
                </Section>
                <Section title={title}>
                  <fieldset>
                    <legend className="visually-hidden">{title}</legend>
                    <DateRangePicker
                      selectedRange={selectedRange}
                      onChange={changeDateRange}
                      onCalendarNavigate={handleCalendarNavigate}
                      maxDateLoaded={maxDateLoaded}
                      now={today}
                      minDate={today}
                      maxDate={maxDate}
                      disabledDates={disabledDates}
                      disabledStartDates={disabledStartDates}
                      disabledEndDates={disabledEndDates}
                      locale={dateLocale || defaultDateLocale}
                      txt={{
                        from: t(label, 'from', locale),
                        until: t(label, 'until', locale),
                        placeholderFrom: t(label, 'undefined', locale),
                        placeholderUntil: t(label, 'undefined', locale)
                      }}
                      className={cx(validationResult.dateRangeErrors ? 'invalid-date-range' : '')}
                      dayButtonClass={cx('opcal__day')}
                      dayContentRenderer={renderDay}
                    />
                  </fieldset>
                  {validationResult.dateRangeErrors &&
                    validationResult.dateRangeErrors.map((msg, i) => (
                      <React.Fragment key={i}>
                        <Warning className="mt-2">{msg}</Warning>
                      </React.Fragment>
                    ))}
                  <div className="mt-3">
                    <div className="form-check form-switch d-inline-block">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        id="show-day-quants"
                        checked={showDayQuants}
                        onChange={changeShowDayQuants}
                      />
                      <label className="form-check-label" htmlFor="show-day-quants">
                        {t(label, 'show-day-quants', locale)}
                      </label>
                    </div>
                  </div>
                </Section>
              </div>
            )}
          </Let>
        )}
      </div>
    </form>
  )
}

OrderPanel.displayName = 'OrderPanel'
OrderPanel.propTypes = orderPanelPropTypes
export default OrderPanel

/**
 * Resolve which pickup location to select after switching inventory pools.
 * Location ids are pool-scoped (never shared across pools); only names can match.
 * Priority: Hauptlager stays → same name on next pool → first alt of next pool
 * (BE already orders by name) → Hauptlager only if the next pool has no locations.
 */
export function resolvePickupLocationOnPoolChange({
  isTransportable,
  selectedPickupLocationId,
  previousPool,
  nextPool
}) {
  if (!isTransportable) return null
  // Hauptlager selected → stay on Hauptlager after pool switch
  if (!selectedPickupLocationId) return null

  const nextLocations = nextPool?.pickupLocations || []
  const previousLocations = previousPool?.pickupLocations || []
  const previousLocation = previousLocations.find(loc => loc.id === selectedPickupLocationId)
  if (previousLocation?.name) {
    const nameMatch = nextLocations.find(loc => loc.name === previousLocation.name)
    if (nameMatch) return nameMatch.id
  }
  return nextLocations[0]?.id || null
}

function getDateRangePickerConstraints(poolAvailability, today, wantedQuantity) {
  const { dates } = poolAvailability
  const getDates = filter => [...dates.filter(filter).map(x => x.parsedDate)]
  return {
    disabledDates: getDates(x => x.quantity < wantedQuantity && x.parsedDate >= today),
    disabledStartDates: getDates(x => x.startDateRestrictions && x.startDateRestrictions.length > 0),
    disabledEndDates: getDates(x => x.endDateRestrictions && x.endDateRestrictions.length > 0)
  }
}

function getMaxQuantityByDay(poolAvailability) {
  return Object.fromEntries(
    poolAvailability.dates.map(x => [formatISO(x.parsedDate, { representation: 'date' }), x.quantity])
  )
}

function getByDay(dateList, date) {
  return dateList.find(x => isSameDay(x.parsedDate, date))
}

function validatePool(inventoryPool, locale, txt) {
  if (inventoryPool.userHasNoAccess) {
    return t(txt, 'no-pool-access', locale)
  }
  if (inventoryPool.userIsSuspended) {
    return t(txt, 'pool-suspension', locale)
  }
  if (!inventoryPool.totalReservableQuantity) {
    return t(txt, 'item-not-available-in-pool', locale)
  }
  if (inventoryPool.isSurrogate) {
    return t(txt, 'unknown-pool', locale)
  }
}

export const validateDateRange = (
  selectedRange,
  today,
  maxDate,
  poolAvailability,
  wantedQuantity,
  locale,
  dateLocale,
  txt,
  alternativePickupLocationSelected = false
) => {
  const { startDate, endDate } = selectedRange
  const { dates, inventoryPool } = poolAvailability
  const {
    reservationAdvanceDays,
    transferBufferBeforePickUp,
    maximumReservationDuration,
    holidays = []
  } = inventoryPool
  const earliestPickupDays = alternativePickupLocationSelected
    ? Math.max(reservationAdvanceDays || 0, transferBufferBeforePickUp || 0)
    : reservationAdvanceDays || 0

  const basicValidityMessage = (() => {
    // Ensure that a valid quantity is given (the quantity field also has its own validator, so this is an exceptional case)
    wantedQuantity = parseInt(wantedQuantity, 10)
    if (Number.isNaN(wantedQuantity) || wantedQuantity < 1) {
      return t(txt, 'missing-quantity', locale)
    }

    // Formal validity of dates (DateRangePicker guarantees for that, so this is an exceptional case)
    if (!isValid(startDate)) {
      return t(txt, 'invalid-start-date', locale)
    }
    if (!isValid(endDate)) {
      return t(txt, 'invalid-end-date', locale)
    }
    if (isBefore(endDate, startDate)) {
      return t(txt, 'start-after-end', locale)
    }
  })()

  if (basicValidityMessage) {
    return [basicValidityMessage]
  }

  // Start date
  const isOneDayPeriod = isSameDay(startDate, endDate)

  const startDateMessage = (() => {
    // Future-only
    if (startDate < today) {
      return t(txt, 'start-date-in-past', locale)
    }

    // Closed pool
    const txtPoolClosed = isOneDayPeriod ? 'pool-closed-at-start-and-end-date' : 'pool-closed-at-start-date'
    const startDateInfo = getByDay(dates, startDate)
    if (startDateInfo) {
      const isRestrictedBy = r => startDateInfo.startDateRestrictions && startDateInfo.startDateRestrictions.includes(r)
      if (isRestrictedBy('HOLIDAY')) {
        const holidayName = holidays.find(
          holiday =>
            startDate >= startOfDay(parseISO(holiday.startDate)) && startDate <= startOfDay(parseISO(holiday.endDate))
        )?.name
        return t(txt, txtPoolClosed, locale, { startDate }) + (holidayName ? ' (' + holidayName + ')' : '')
      } else if (isRestrictedBy('NON_WORKDAY')) {
        const dayName = format(startDate, 'EEEE', { locale: dateLocale })
        return (
          t(txt, txtPoolClosed, locale, { startDate }) +
          (dayName ? ' (' + t(txt, 'closed-on-day-of-week', locale, { dayName }) + ')' : '')
        )
      } else if (isRestrictedBy('VISITS_CAPACITY_REACHED')) {
        return t(txt, txtPoolClosed, locale, { startDate }) + t(txt, 'pool-closed-max-visits', locale)
      } else if (isRestrictedBy('BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE')) {
        // (This case should have been prevented by the future-only rule above)
        return t(txt, 'start-date-not-before', locale, { days: earliestPickupDays })
      }
    }
  })()

  const endDateMessage = (() => {
    // Max date
    if (endDate > maxDate) {
      return t(txt, 'end-date-too-late', locale, { maxDate })
    }

    // Closed pool
    if (isOneDayPeriod) {
      // (don't repeat the startDateMessage for the same day)
      return
    }
    if (endDate < today) {
      // (report issues only for non-past dates)
      return
    }
    const endDateInfo = getByDay(dates, endDate)
    if (endDateInfo) {
      const isRestrictedBy = r => endDateInfo.endDateRestrictions && endDateInfo.endDateRestrictions.includes(r)
      if (isRestrictedBy('HOLIDAY')) {
        const holidayName = holidays.find(
          h => endDate >= startOfDay(parseISO(h.startDate)) && endDate <= startOfDay(parseISO(h.endDate))
        )?.name
        return t(txt, 'pool-closed-at-end-date', locale, { endDate }) + (holidayName ? ' (' + holidayName + ')' : '')
      } else if (isRestrictedBy('NON_WORKDAY')) {
        const dayName = format(endDate, 'EEEE', { locale: dateLocale })
        return (
          t(txt, 'pool-closed-at-end-date', locale, { endDate }) +
          (dayName ? ' (' + t(txt, 'closed-on-day-of-week', locale, { dayName }) + ')' : '')
        )
      } else if (isRestrictedBy('VISITS_CAPACITY_REACHED')) {
        return t(txt, 'pool-closed-at-end-date', locale, { endDate }) + t(txt, 'pool-closed-max-visits', locale)
      }
    }
  })()

  // Available quantity
  const availabilityMessage = (() => {
    const noAvailDates = [
      ...eachDayOfInterval({ start: startDate, end: endDate }).filter(d => {
        if (d < today) {
          // (report issues only for non-past dates)
          return false
        }
        const dateInfo = getByDay(dates, d)
        return dateInfo && dateInfo.quantity < wantedQuantity
      })
    ]
    if (noAvailDates.length > 0) {
      if (noAvailDates.length === 1) {
        const startDate = noAvailDates[0]
        return t(txt, 'quantity-to-large-at-day', locale, { startDate })
      } else {
        return t(txt, 'quantity-to-large-in-range', locale, {})
      }
    }
  })()

  // Max reservation time
  const maximumReservationDurationMessage = (() => {
    if (maximumReservationDuration) {
      const maxEndDate = addDays(startDate, maximumReservationDuration - 1)
      if (endDate > maxEndDate) {
        return t(txt, 'maximum-reservation-duration', locale, { days: maximumReservationDuration })
      }
    }
  })()

  return [
    ...[startDateMessage, endDateMessage, availabilityMessage, maximumReservationDurationMessage].filter(x => !!x)
  ]
}
