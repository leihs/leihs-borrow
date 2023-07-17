import React from 'react'
import SquareImageGrid from './SquareImageGrid'

export default {
  title: 'Design Components/Content/SquareImageGrid',
  component: SquareImageGrid
}

export const squareImageGrid = () => {
  return (
    <div>
      <h1>SquareImageGrid</h1>
      <SquareImageGrid {...listGridProps} />
    </div>
  )
}
squareImageGrid.storyName = 'SquareImageGrid'

export const restProps = () => {
  return (
    <div>
      <h1>SquareImageGrid</h1>
      <p className="text-muted">Set arbitrary attributes with restProps</p>
      <SquareImageGrid {...listGridProps} className="text-primary" />
    </div>
  )
}

const listGridProps = {
  list: [
    {
      id: 1,
      href: null,
      imgSrc: require('../../static/example-images/models/e2d0cfdf-745c-57cb-8c6b-09c14af6bb51.jpg'),
      caption: 'Item 1'
    },
    {
      id: 2,
      href: null,
      imgSrc: require('../../static/example-images/models/4f64adb0-6325-4eb1-bf36-8a73a986ed4b.jpg'),
      caption: 'Item 2 (a favorite)',
      subCaption: 'it is a favorite',
      isFavorited: true
    },
    {
      id: 3,
      href: null,
      imgSrc: null,
      caption: 'Item 3',
      subCaption: 'it is dimmed',
      isDimmed: true
    },
    {
      id: 4,
      href: null,
      imgSrc: null,
      caption: 'Item 4 with much text text text'
    }
  ]
}
