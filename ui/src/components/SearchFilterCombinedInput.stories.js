import React, { useState } from 'react'
import SearchFilterCombinedInput from './SearchFilterCombinedInput'

export default {
  title: 'Design Components/Form Controls/SearchFilterCombinedInput',
  component: SearchFilterCombinedInput
}

export const searchFilterCombinedInput = () => {
  const [searchTerm, setSearchTerm] = useState('')

  return (
    <div>
      <h1>SearchFilterCombinedInput</h1>
      <p className="text-muted">A text search field combined with a submit button</p>
      <div className="d-grid gap-3">
        <SearchFilterCombinedInput
          searchTerm={searchTerm}
          onSearchTermChange={v => setSearchTerm(v)}
          onSearchTermClear={() => setSearchTerm('')}
          searchLabel="Suchen"
          searchPlaceholder="Suchbegriff"
        />
        <input
          type="text"
          name="mirror"
          placeholder="Input bound to same state"
          className="form-control"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
        />
      </div>
    </div>
  )
}
searchFilterCombinedInput.storyName = 'SearchFilterCombinedInput'
