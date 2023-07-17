import React from 'react'
import SquareImage from './SquareImage'

export default {
  title: 'Design Components/Content/SquareImage',
  component: SquareImage
}

const imgSrcLandscape = require('../../static/example-images/categories/2b4c8bd3-3d65-5e68-bf7a-3649ec67a1a2.jpg')
const imgSrcPortrait = require('../../static/example-images/models/62f4cb3c-999d-53ec-9426-298ebacd208a.jpg')

export const squareImage = () => {
  return (
    <div>
      <h1>SquareImage</h1>
      <p className="text-muted">Shows an image centered in a full width square box regardless of its size and ratio.</p>
      <p className="text-muted">(Note that the border is set via className for illustrative purposes)</p>
      <div style={{ maxWidth: '200px' }}>
        <SquareImage imgSrc={imgSrcLandscape} className="border border-gray-200 mb-3" />
        <SquareImage imgSrc={imgSrcPortrait} className="border border-gray-200 mb-3" />
      </div>
      <p className="text-muted">Example images used (landscape + portrait):</p>
      <img src={imgSrcLandscape} className="border me-3" style={{ width: '100px' }} alt="Example" />
      <img src={imgSrcPortrait} className="border" style={{ height: '100px' }} alt="Example" />
    </div>
  )
}
squareImage.storyName = 'SquareImage'

export const placeholder = () => {
  return (
    <div>
      <h1>SquareImage</h1>
      <p className="text-muted">When no imgSrc prop is provided, a gray space is rendered as the default placeholder</p>
      <SquareImage className="border border-gray-200 mb-3" />
      <p className="text-muted">The placeholder can also be set via props (shown centered)</p>
      <SquareImage
        className="border border-gray-200"
        placeholder={<div className="shadow">whatever placeholder you want</div>}
      />
    </div>
  )
}

export const link = () => {
  return (
    <div>
      <h1>SquareImage</h1>
      <p className="text-muted">The href attribute can be used to link the image</p>
      <SquareImage imgSrc={imgSrcLandscape} className="border border-gray-200" href="" />
    </div>
  )
}

export const restProps = () => {
  return (
    <div>
      <h1>SquareImage</h1>
      <p className="text-muted">Set arbitrary attributes with restProps</p>
      <SquareImage imgSrc={imgSrcLandscape} className="border border-primary" />
    </div>
  )
}

export const className = () => {
  return (
    <div>
      <h1>SquareImage</h1>
      <p className="text-muted">
        Override styling with <code>className</code> and <code>imgClassName</code>
      </p>
      <SquareImage imgSrc={imgSrcPortrait} className="shadow mb-4" imgClassName="bg-light-shade" />
      <p className="text-muted">
        Example with padding 0 (note that this has to be applied via <code>paddingClassName</code> to override the
        built-in padding of 2):
      </p>
      <SquareImage imgSrc={imgSrcPortrait} className="border" paddingClassName="p-0" />
    </div>
  )
}
