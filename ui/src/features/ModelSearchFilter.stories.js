import React from 'react'
import { linkTo } from '@storybook/addon-links'
import ModelSearchFilter from './ModelSearchFilter'

export default {
  title: 'Feature Components/ModelSearchFilter',
  component: ModelSearchFilter
}

export const modelSearchFilter = () => (
  <div>
    <p className="text-muted">Siehe</p>
    <button className="btn btn-light btn-sm" onClick={linkTo('Prototypes/Catalog/Search Results')}>
      Prototypes &gt; Katalog &gt; Suchresultate
    </button>
  </div>
)

modelSearchFilter.storyName = 'ModelSearchFilter'
