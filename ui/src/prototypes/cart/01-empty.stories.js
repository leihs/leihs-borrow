import React from 'react'

import PageLayout from '../../components/PageLayout'
import PageLayoutMock from '../../story-utils/PageLayoutMock'

export default {
  title: 'Prototypes/Cart/Empty',
  parameters: { layout: 'fullscreen' }
}

export const empty = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Warenkorb"></PageLayout.Header>
      <div className="d-grid gap-4 text-center decorate-links">
        Noch keine Gegenstände hinzugefügt
        <a href="/borrow/">Hier geht&apos;s zum Katalog</a>
      </div>
    </PageLayoutMock>
  )
}
