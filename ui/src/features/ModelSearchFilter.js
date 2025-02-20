import React, { useState } from 'react'
import cx from 'classnames'
import { parseISO as parseDate } from 'date-fns'
import { CrossIcon } from '../components/Icons'
import SearchFilterCombinedInput from '../components/SearchFilterCombinedInput'

import { translate as t } from '../lib/translate'

const BASE_CLASS = 'ui-model-search-filter'

export default function ModelSearchFilter({
  className,
  availableFilters = {},
  currentFilters = {},
  locale,
  txt,
  onSubmitTerm,
  onTriggerAvailability,
  onClearFilter,
  onChangePool = () => {},
  ...restProps
}) {
  const { term = '', selectedPool, onlyAvailable = false, quantity = 1, startDate, endDate } = currentFilters
  const { pools: availablePools = [] } = availableFilters

  const [searchTerm, setSearchTerm] = useState(term || '')
  const handleTermChange = str => {
    setSearchTerm(str)
  }
  const handleTermClear = () => {
    onClearFilter({ type: 'term' })
  }

  const [poolId, setPoolId] = useState(selectedPool?.id || '')
  const handlePoolChange = e => {
    setPoolId(e.target.value)
    onChangePool(e.target.value)
  }

  return (
    <div className={cx(BASE_CLASS, className)} {...restProps}>
      <form
        action="#"
        role="search"
        method="get"
        onSubmit={e => {
          e.preventDefault()
          onSubmitTerm(searchTerm)
        }}
      >
        <SearchFilterCombinedInput
          searchTerm={searchTerm}
          onSearchTermChange={handleTermChange}
          onSearchTermClear={handleTermClear}
          searchLabel={t(txt, 'search-button-label', locale)}
          searchPlaceholder={t(txt, 'search-input-placeholder', locale)}
        />

        <div className="filters">
          <label className="visually-hidden" htmlFor="pool">
            {t(txt, 'pool-select-label', locale)}
          </label>

          {/* Inventory Pools */}
          {availablePools.length > 0 && (
            <div className="filters--item input-group">
              <select
                className="form-select filter-input"
                id="pool"
                name="pool"
                value={poolId}
                onChange={handlePoolChange}
                tabIndex="2"
              >
                {availablePools.map(pool => (
                  <option key={pool.id} value={pool.id}>
                    {pool.label}
                  </option>
                ))}
              </select>
              {poolId && (
                <button
                  type="button"
                  className="btn btn-secondary bg-light-shade filter-input filter-input--clear-button"
                  onMouseDown={e => e.preventDefault()}
                  onClick={e => {
                    e.stopPropagation()
                    onClearFilter({ type: 'pool' })
                  }}
                  aria-label="Clear filter"
                >
                  <CrossIcon height="14" width="14" />
                </button>
              )}
            </div>
          )}

          {/* Availability */}
          <div className="filters--item btn-group">
            <button
              type="button"
              id="availability"
              name="availability"
              className={cx('btn btn-secondary fw-bold bg-light-shade filter-input text-nowrap', {
                'calendar-indicator': !onlyAvailable
              })}
              onClick={onTriggerAvailability}
              tabIndex="3"
              aria-label={t(txt, 'availability-button-label', locale)}
            >
              {onlyAvailable
                ? t(txt, 'availability-label', locale, {
                    startDate: parseDate(startDate),
                    endDate: parseDate(endDate),
                    quantity
                  })
                : t(txt, 'availability-unrestricted', locale)}
            </button>
            {onlyAvailable && (
              <button
                type="button"
                className="btn btn-secondary bg-light-shade filter-input filter-input--clear-button"
                onMouseDown={e => e.preventDefault()}
                onClick={e => {
                  e.stopPropagation()
                  onClearFilter({ type: 'onlyAvailable' })
                }}
                aria-label="Clear filter"
              >
                <CrossIcon height="14" width="14" />
              </button>
            )}
          </div>
        </div>
      </form>
    </div>
  )
}
