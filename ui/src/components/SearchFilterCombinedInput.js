import React from 'react'
import PropTypes from 'prop-types'
import InputWithClearButton from '../components/InputWithClearButton'

export default function SearchFilterCombinedInput({
  searchTerm,
  onSearchTermChange,
  onSearchTermClear,
  searchLabel,
  searchPlaceholder
}) {
  return (
    <div className="d-flex">
      <div style={{ flex: '1 0 auto' }}>
        <InputWithClearButton
          className="form-control border border-primary rounded-0 rounded-start"
          name="term"
          title={searchLabel}
          value={searchTerm}
          onChange={e => onSearchTermChange(e.target.value)}
          onClear={onSearchTermClear}
          placeholder={searchPlaceholder}
          aria-autocomplete="both"
          autoComplete="off"
          autoCapitalize="off"
          autoCorrect="off"
          autoFocus=""
          spellCheck="false"
          tabIndex="1"
        />
      </div>
      <button type="submit" className="btn btn-primary rounded-0 rounded-end" aria-label={searchLabel}>
        {searchLabel}
      </button>
    </div>
  )
}

SearchFilterCombinedInput.propTypes = {
  searchTerm: PropTypes.string,
  onSearchTermChange: PropTypes.func.isRequired,
  onSearchTermClear: PropTypes.func.isRequired,
  searchLabel: PropTypes.string.isRequired,
  searchPlaceholder: PropTypes.string.isRequired
}
