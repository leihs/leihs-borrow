import React from 'react'

import PageLayout from '../../components/PageLayout'
import Stack from '../../components/Stack'
import PageLayoutMock from '../../story-utils/PageLayoutMock'

export default {
  title: 'Prototypes/Cart/Empty',
  parameters: { layout: 'fullscreen' }
}

export const empty = () => {
  return (
    <PageLayoutMock>
      <PageLayout.Header title="Warenkorb"></PageLayout.Header>
      <Stack space="4" className="text-center decorate-links">
        Noch keine Gegenstände hinzugefügt
        <a href="/borrow/">Hier geht&apos;s zum Katalog</a>
      </Stack>
    </PageLayoutMock>
  )
}
