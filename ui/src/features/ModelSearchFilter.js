import React, { useState } from 'react'
import cx from 'classnames'
import { parseISO as parseDate } from 'date-fns'
import Icon, { iconFilter, iconCircleCross } from '../components/Icons'

import { translate as t } from '../lib/translate'

const BASE_CLASS = 'ui-model-search-filter'

export default function ModelSearchFilter({
  className,
  availableFilters = {},
  currentFilters = {},
  locale,
  txt,
  onChangeSearchTerm,
  onSubmit,
  onOpenPanel,
  onClearFilter,
  ...restProps
}) {
  const { term = '', poolIds = [], onlyAvailable = false, quantity = 1, startDate, endDate } = currentFilters
  const [searchTerm, setSearchTerm] = useState(term || '')
  const onChangeTerm = str => {
    setSearchTerm(str)
    onChangeSearchTerm(str)
  }
  return (
    <div className={cx(BASE_CLASS, className)} {...restProps}>
      <form
        action="#"
        role="search"
        method="get"
        onSubmit={e => {
          e.preventDefault()
          onSubmit({ searchTerm })
        }}
      >
        <SearchFilterCombinedInput
          onFilterClick={onOpenPanel}
          searchTerm={searchTerm}
          onSearchTermChange={onChangeTerm}
          searchLabel={t(txt, 'search-input-label', locale)}
          searchPlaceholder={t(txt, 'search-input-placeholder', locale)}
          filterLabel={t(txt, 'search-filter-label', locale)}
        />

        {!!poolIds &&
          poolIds.map(
            pool =>
              !!pool && (
                <FilterItemButton key={pool.id} onFilterClick={onOpenPanel} onClear={() => onClearFilter(pool)}>
                  {pool.label || pool.id}
                </FilterItemButton>
              )
          )}

        {!!onlyAvailable && (
          <FilterItemButton onFilterClick={onOpenPanel} onClear={() => onClearFilter({ type: 'onlyAvailable' })}>
            {t(txt, 'availability-label', locale, {
              startDate: parseDate(startDate),
              endDate: parseDate(endDate),
              quantity
            })}
          </FilterItemButton>
        )}
      </form>
    </div>
  )
}

function SearchFilterCombinedInput({
  searchTerm,
  onSearchTermChange,
  filterLabel,
  searchLabel,
  searchPlaceholder,
  onFilterClick
}) {
  return (
    <div className="mb-2 position-relative">
      <input
        type="text"
        className="form-control border border-primary"
        style={{ paddingRight: '150px' }}
        name="term"
        title={searchLabel}
        value={searchTerm}
        onChange={e => onSearchTermChange(e.target.value)}
        placeholder={searchPlaceholder}
        aria-autocomplete="both"
        autoComplete="off"
        autoCapitalize="off"
        autoCorrect="off"
        autoFocus=""
        spellCheck="false"
        tabIndex="1"
      />
      <div className="position-absolute" style={{ top: '-2px', right: '-2px' }}>
        <button
          type="button"
          onClick={onFilterClick}
          aria-label={filterLabel}
          title={filterLabel}
          className="btn"
          tabIndex="2"
        >
          <Icon icon={iconFilter} />
        </button>
        <button type="submit" className="btn btn-primary rounded-0 rounded-end" aria-label={searchLabel} tabIndex="3">
          {searchLabel}
        </button>
      </div>
    </div>
  )
}

// shows 1 filter item in a bubble with a "clear" button
function FilterItemButton({ children, onFilterClick, onClear, ...restProps }) {
  return (
    <FilterBubble {...restProps} className="ps-3 pe-2" style={{ borderRadius: '1rem' }}>
      <span role="button" onClick={onFilterClick}>
        {children}
      </span>
      <span role="button">
        <Icon
          icon={iconCircleCross}
          style={{ marginLeft: '10px', marginRight: '0px', marginTop: '-3px' }}
          height="14"
          width="14"
          onClick={e => {
            e.stopPropagation()
            onClear()
          }}
        ></Icon>
      </span>
    </FilterBubble>
  )
}

function FilterBubble({ children, className, style, as: Elm = 'div', ...restProps }) {
  return (
    <Elm
      className={cx('ui-filter-bubble', 'btn btn-primary btn-sm rounded-pill very-small mb-1 me-1 fw-bold', className)}
      style={{ paddingTop: '7px', paddingBottom: '6px', ...style }}
      {...restProps}
    >
      {children}
    </Elm>
  )
}
