import React, { useState, useEffect } from 'react'

import PageLayoutMock from '../story-utils/PageLayoutMock'
import Stack from '../components/Stack'
import ErrorNotification from '../components/ErrorNotification'
import ErrorView from '../components/ErrorView'

export default {
  title: 'Prototypes/Errors',
  parameters: {
    layout: 'fullscreen'
  }
}

function Crasher() {
  const [crash, setCrash] = useState(false)
  useEffect(() => {
    if (crash) {
      throw new Error('It crashed')
    }
  }, [crash])
  return (
    <>
      <button type="button" className="btn btn-primary" onClick={() => setCrash(true)}>
        Crash this component
      </button>
    </>
  )
}

export const errors = () => {
  const [showErrorView, setShowErrorView] = useState()
  const [showErrorView401, setShowErrorView401] = useState()
  const [showErrorNotification, setShowErrorNotification] = useState()
  return (
    <PageLayoutMock>
      <h1>Errors</h1>
      <Stack space="4">
        <div>
          <h2>Initial fetching (routing)</h2>
          <p className="text-muted">
            Inline error message is shown in place of the content which is affected by the error. Depending on the
            situation the message can replace the whole page content or only parts of it.
          </p>
          <p>
            <button type="button" className="btn btn-primary" onClick={() => setShowErrorView(x => !x)}>
              Load something
            </button>
          </p>
          {showErrorView && (
            <ErrorView
              title="Error when loading content"
              details={'- bar\n- baz'}
              actions={[{ title: 'Reload current page', onClick: () => document.location.reload(), variant: 'button' }]}
            />
          )}
          <p className="text-muted">Variant: unauthorized</p>
          <p>
            <button type="button" className="btn btn-primary" onClick={() => setShowErrorView401(x => !x)}>
              Load something (unauthorized)
            </button>
          </p>
          {showErrorView401 && (
            <ErrorView
              title="Error when loading content"
              message="Not logged in"
              details={'- bar\n- baz'}
              actions={[{ title: 'Go to log-in page', href: '/sign-in', variant: 'button' }]}
            />
          )}
        </div>

        <div>
          <h2>Non-routing events</h2>
          <p className="text-muted">A modal notification is shown. </p>
          <p>
            <button type="button" className="btn btn-primary" onClick={() => setShowErrorNotification(true)}>
              Do something
            </button>
          </p>
          {showErrorNotification && (
            <ErrorNotification shown title="Error" onDismiss={() => setShowErrorNotification(false)}>
              <div>Error when processing this action</div>
              <details className="mt-4 mb-4">
                <div className="preserve-linebreaks mt-3 small">Server response: 404 not found</div>
              </details>
            </ErrorNotification>
          )}
        </div>

        <div>
          <h2>Component rendering</h2>
          <p className="text-muted">
            Component exceptions are caught and displayed by the layout&apos;s ErrorBoundary.
          </p>
          <Crasher />
          <p className="text-muted small mt-1">
            Ignore the dark-coloured overlay that is shown first, it does not exist in production
          </p>
        </div>
      </Stack>
    </PageLayoutMock>
  )
}
