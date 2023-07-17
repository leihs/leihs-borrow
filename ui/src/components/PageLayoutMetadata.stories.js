import React from 'react'
import PageLayout from './PageLayout'

export default {
  title: 'Design Components/Layout/PageLayoutMetadata',
  component: PageLayout.Metadata
}

const loremText = `Page content - Lorem ipsum dolor sit amet consectetur adipisicing elit. Delectus debitis voluptatum quod
pariatur dolorum adipisci itaque tempora consequuntur assumenda odit quas sequi, eos provident sit earum
molestias quibusdam voluptates numquam.`

export const pageLayoutMetadata = () => {
  return (
    <div>
      <h1>PageLayout.Metadata</h1>
      <p className="text-muted">A unobtrusive element to show metadata in the bottom of a page where needed.</p>
      <div>{loremText}</div>
      <PageLayout.Metadata>ID: 12345</PageLayout.Metadata>
    </div>
  )
}
pageLayoutMetadata.storyName = 'PageLayout.Metadata'

export const pageLayoutMetadataWithDetailsJSON = () => {
  return (
    <div>
      <h1>PageLayout.Metadata</h1>
      <p className="text-muted">A unobtrusive element to show metadata in the bottom of a page where needed.</p>
      <div>{loremText}</div>
      <PageLayout.Metadata summary="ID: 12345" details={{ foo: 'bar', baz: { a: 1 } }}></PageLayout.Metadata>
    </div>
  )
}
pageLayoutMetadataWithDetailsJSON.storyName = 'PageLayout.MetadataWithDetails (JSON)'

export const pageLayoutMetadataWithDetailsText = () => {
  return (
    <div>
      <h1>PageLayout.Metadata</h1>
      <p className="text-muted">A unobtrusive element to show metadata in the bottom of a page where needed.</p>
      <div>{loremText}</div>
      <PageLayout.Metadata
        summary="ID: 12345"
        details={'here are some details about the page that are initially hidden'}
      ></PageLayout.Metadata>
    </div>
  )
}
pageLayoutMetadataWithDetailsText.storyName = 'PageLayout.MetadataWithDetails (text)'

export const restProps = () => {
  return (
    <div>
      <h1>PageLayout.Metadata</h1>
      <p className="text-muted">RestProps</p>
      <PageLayout.Metadata className="border border-primary">Metadata content</PageLayout.Metadata>
    </div>
  )
}
