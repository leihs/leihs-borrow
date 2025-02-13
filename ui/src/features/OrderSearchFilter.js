import React, { useState } from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import { parseISO as parseDate } from 'date-fns'
import { CrossIcon } from '../components/Icons'
import SearchFilterCombinedInput from '../components/SearchFilterCombinedInput'
import { translate as t } from '../lib/translate'

const BASE_CLASS = 'ui-order-search-filter'

export default function OrderSearchFilter({
  className,
  availableFilters = {},
  currentFilters = {},
  locale,
  txt,
  onSubmitTerm,
  onTriggerTimespan,
  onClearFilter,
  onChangePool,
  ...restProps
}) {
  const { term = '', selectedPool, from, until } = currentFilters
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

  const timespanLabel = (function foo() {
    if (from) {
      if (until) {
        return t(txt, 'timespan-label', locale, {
          startDate: parseDate(from),
          endDate: parseDate(until)
        })
      }
      return t(txt, 'timespan-label-from', locale, {
        startDate: parseDate(from)
      })
    } else {
      if (until) {
        return t(txt, 'timespan-label-until', locale, {
          endDate: parseDate(until)
        })
      }
      return t(txt, 'timespan-unrestricted', locale)
    }
  })()

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
          {/* Inventory Pools */}
          <label className="visually-hidden" htmlFor="pool">
            {t(txt, 'pool-select-label', locale)}
          </label>
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

          {/* Timespan */}
          <div className="filters--item btn-group">
            <button
              type="button"
              id="timespan"
              name="timespan"
              className={cx('btn btn-secondary fw-bold bg-light-shade filter-input text-nowrap', {
                'calendar-indicator': !from && !until
              })}
              onClick={onTriggerTimespan}
              tabIndex="3"
              aria-label={t(txt, 'timespan-button-label', locale)}
            >
              {timespanLabel}
            </button>
            {(from || until) && (
              <button
                type="button"
                className="btn btn-secondary bg-light-shade filter-input filter-input--clear-button"
                onMouseDown={e => e.preventDefault()}
                onClick={e => {
                  e.stopPropagation()
                  onClearFilter({ type: 'timespan' })
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

OrderSearchFilter.propTypes = {
  className: PropTypes.string,
  availableFilters: PropTypes.object,
  currentFilters: PropTypes.object,
  locale: PropTypes.string,
  txt: PropTypes.object,
  onSubmitTerm: PropTypes.func.isRequired,
  onTriggerTimespan: PropTypes.func.isRequired,
  onClearFilter: PropTypes.func.isRequired,
  onChangePool: PropTypes.func.isRequired
}
