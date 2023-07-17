import React from 'react'
import PropertyTable from './PropertyTable'

export default {
  title: 'Design Components/Content/PropertyTable',
  component: PropertyTable
}

export const keyValueTable = () => {
  return (
    <div>
      <h1>PropertyTable</h1>
      <p className="text-muted">E.g. for user details or specifications of a model</p>
      <PropertyTable
        properties={[
          { key: 'Age', value: '45' },
          { key: 'Weight', value: '80 kg' }
        ]}
      />
    </div>
  )
}
keyValueTable.storyName = 'PropertyTable'

export const withLongContent = () => {
  return (
    <div>
      <h1>PropertyTable</h1>
      <p className="text-muted">Short keys, long content</p>
      <PropertyTable
        className="shadow"
        style={{ maxWidth: '40rem' }}
        properties={[
          {
            key: 'Key 1',
            value: 'Lorem ipsum dolor sit amet consectetur adipisicing elit. Et maiores cumque in nisi natus id saepe!'
          },
          {
            key: 'Key 2',
            value: 'Et maiores cumque in nisi natus id saepe!'
          }
        ]}
      />
      <p></p>
      <p className="text-muted">Long keys, short content</p>
      <PropertyTable
        className="shadow"
        style={{ maxWidth: '40rem' }}
        properties={[
          {
            key: 'Lorem ipsum dolor sit amet consectetur adipisicing elit. Et maiores cumque in nisi natus id saepe!',
            value: 'Value 1'
          },
          {
            key: 'Et maiores cumque in nisi natus id saepe!',
            value: 'Value 2'
          }
        ]}
      />
      <p></p>
      <p className="text-muted">Both mixed</p>
      <PropertyTable
        className="shadow"
        style={{ maxWidth: '40rem' }}
        properties={[
          {
            key: 'Key 1',
            value: 'Lorem ipsum dolor sit amet consectetur adipisicing elit. Et maiores cumque in nisi natus id saepe!'
          },
          {
            key: 'Et maiores cumque in nisi natus id saepe!',
            value: 'Value 2'
          }
        ]}
      />
      <p></p>
      <p className="text-muted">Long words</p>
      <PropertyTable
        className="shadow"
        style={{ maxWidth: '20rem' }}
        properties={[
          {
            key: 'supercalifragilisticexpialidocious',
            value: 'supercalifragilisticexpialidocious'
          }
        ]}
      />
      <p></p>
    </div>
  )
}

export const restProps = () => {
  return (
    <div>
      <h1>PropertyTable</h1>
      <p className="text-muted">Set arbitrary attributes with restProps</p>
      <PropertyTable
        className="border border-primary"
        properties={[
          { key: 'Age', value: '45' },
          { key: 'Weight', value: '80 kg' }
        ]}
      />
    </div>
  )
}
