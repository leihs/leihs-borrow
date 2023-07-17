import React from 'react'
import FilterButton from './FilterButton'

export default {
  title: 'Design Components/Content/FilterButton',
  component: FilterButton,
  argTypes: { onClick: { action: 'click' } }
}

export const filterButton = ({ onClick }) => {
  return (
    <div>
      <h1>FilterButton</h1>
      <p className="text-muted">It is a button as well as an indicator what filter is currently active.</p>
      <FilterButton onClick={onClick}>Verfügbar ab 8.6.2021</FilterButton>
    </div>
  )
}
filterButton.storyName = 'FilterButton'

export const multiLine = ({ onClick }) => {
  return (
    <div>
      <h1>FilterButton</h1>
      <p className="text-muted">It can have multiline content</p>
      <FilterButton onClick={onClick}>
        <div>Lorem ipsum dolor sit amet</div>
        <div>consetetur sadipscing elitr</div>
      </FilterButton>
    </div>
  )
}

export const restProps = ({ onClick }) => {
  return (
    <div>
      <h1>FilterButton</h1>
      <p className="text-muted">RestProps can be applied</p>
      <FilterButton onClick={onClick} className="btn-warning">
        Button with warning color
      </FilterButton>
    </div>
  )
}
