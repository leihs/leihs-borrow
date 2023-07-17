import React from 'react'
import Spinner from './Spinner'

export default {
  title: 'Design Components/Content/Spinner',
  component: Spinner
}

export const spinner = () => {
  return (
    <div>
      <h1>Spinner</h1>
      <p className="text-muted">An animation indicating that the application is doing something.</p>
      <div className="d-flex flex-column gap-3 mb-4">
        <Spinner />
      </div>
      <p className="text-muted">Inside a button:</p>
      <div className="d-flex flex-column gap-3 mb-4">
        <button type="button" className="btn btn-primary" disabled>
          <Spinner /> Saving
        </button>
      </div>
      <p className="text-muted">Example with &quot;rest props&quot;:</p>
      <div className="d-flex flex-column gap-3 mb-4">
        <Spinner className="m-auto" data-example="example" />
      </div>
    </div>
  )
}
spinner.storyName = 'Spinner'
