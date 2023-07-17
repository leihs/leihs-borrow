import React from 'react'
import PageLayout from './PageLayout'
import FilterButton from './FilterButton'

export default {
  title: 'Design Components/Layout/PageLayoutHeader',
  component: PageLayout.Header,
  argTypes: { onClick: { action: 'filter click' } }
}

export const pageLayoutHeader = () => {
  return (
    <div>
      <h1>PageLayout.Header</h1>
      <p className="text-muted">Title and optional head content, centered, with bottom margin:</p>
      <PageLayout.Header preTitle="Pre-Title" title="Title">
        More head content
      </PageLayout.Header>
      Page content begins here
    </div>
  )
}
pageLayoutHeader.storyName = 'PageLayout.Header'

export const typicalExample = ({ onClick }) => {
  return (
    <div>
      <h1>PageLayout.Header</h1>
      <p className="text-muted">Typical example:</p>
      <PageLayout.Header preTitle="Audio" title="Mischpulte & CD Player">
        <FilterButton onClick={onClick}>FilterButton</FilterButton>
      </PageLayout.Header>
    </div>
  )
}

export const variants = () => {
  return (
    <div>
      <h1>PageLayout.Header</h1>
      <p className="text-muted">Title only:</p>
      <PageLayout.Header title="Title"></PageLayout.Header>
      <p className="text-muted">Pre-Title only:</p>
      <PageLayout.Header preTitle="Pre-Title"></PageLayout.Header>
      <p className="text-muted">Head content only:</p>
      <PageLayout.Header>Head content</PageLayout.Header>
      <p className="text-muted">RestProps:</p>
      <PageLayout.Header title="Title" className="border border-primary">
        Head content
      </PageLayout.Header>
    </div>
  )
}
