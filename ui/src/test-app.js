import React from 'react'
import ReactDOM from 'react-dom'
import './scss/styles.scss'
import PageLayoutMock from './story-utils/PageLayoutMock'
import PageLayout from './components/PageLayout'

// React 18: import ReactDOM from 'react-dom/client';

function App() {
  return (
    <>
      <PageLayoutMock>
        <PageLayout.Header title="Borrow UI Test App">
          <p>This is only a debugging tool for the library build. Check the README for more info.</p>
        </PageLayout.Header>
      </PageLayoutMock>
    </>
  )
}

const container = document.getElementById('root')

// eslint-disable-next-line react/no-deprecated
ReactDOM.render(<App />, container)

// React 18:
// const root = ReactDOM.createRoot(container);
// root.render(<App />);
