import React from 'react'

export default {
  title: 'Theme/Colors'
}

export const designColors = () => {
  return (
    <div>
      <h1>Reference: Design colors</h1>
      <div className="mb-1 p-2 doc-design-color-blue text-light">$blue</div>
      <div className="mb-1 p-2 doc-design-color-red text-light">$red</div>
      <div className="mb-1 p-2 doc-design-color-light-shade">$light-shade</div>
      <div className="mb-1 p-2 doc-design-color-light-gray">$light-gray</div>
      <div className="mb-1 p-2 doc-design-color-gray">$gray</div>
      <p className="text-muted">For reference only, do not use directly!</p>
    </div>
  )
}

export const grayScale = () => {
  return (
    <div>
      <h1>Reference: Gray scale</h1>
      <div className="my-1 p-2 doc-bg-gray-100">$gray-100</div>
      <div className="my-1 p-2 doc-bg-gray-200">$gray-200</div>
      <div className="my-1 p-2 doc-bg-gray-300">$gray-300</div>
      <div className="my-1 p-2 doc-bg-gray-400">$gray-400</div>
      <div className="my-1 p-2 doc-bg-gray-500">$gray-500</div>
      <div className="my-1 p-2 doc-bg-gray-600 text-light">$gray-600</div>
      <div className="my-1 p-2 doc-bg-gray-700 text-light">$gray-700</div>
      <div className="my-1 p-2 doc-bg-gray-800 text-light">$gray-800</div>
      <div className="my-1 p-2 doc-bg-gray-900 text-light">$gray-900</div>
      <p className="text-muted">
        Bootstraps&apos;s default grays are slightly blueish, while this design uses neutral grays.
      </p>
      <p className="text-muted">For reference only, do not use directly!</p>
    </div>
  )
}
