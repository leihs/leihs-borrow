import React from 'react'

export default {
  title: 'Theme/Backgrounds'
}

export const backgrounds = () => {
  return (
    <div>
      <h1>Background Utility</h1>
      <p className="text-muted">The theme extends Bootstraps&apos;s background utility by two design colors:</p>
      <div className="my-1 p-2 bg-light-shade">.bg-light-shade</div>
      <div className="my-1 mb-4 p-2 bg-light-gray">.bg-light-gray</div>
      <p className="text-muted">
        The default Bootstrap theme backgrounds are also available (however the design does not explicitly use them):
      </p>
      <div className="my-1 p-1 bg-primary text-light">.bg-primary</div>
      <div className="my-1 p-1 bg-secondary">.bg-secondary</div>
      <div className="my-1 p-1 bg-info">.bg-info</div>
      <div className="my-1 p-1 bg-warning text-light">.bg-warning</div>
      <div className="my-1 p-1 bg-danger">.bg-danger</div>
      <div className="my-1 p-1 bg-light">.bg-light</div>
      <div className="my-1 p-1 bg-dark text-light">.bg-dark</div>
      <div className="my-1 p-1 bg-body">.bg-body</div>
      <div className="my-1 p-1 bg-white">.bg-white</div>
      <div className="my-1 p-1 bg-transparent">.bg-transparent</div>
    </div>
  )
}
