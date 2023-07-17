import React from 'react'
import { action } from '@storybook/addon-actions'
import Badge from './Badge'

export default {
  title: 'Design Components/Content/Badge',
  component: Badge
}

export const badge = () => {
  return (
    <div>
      <h1>Badge</h1>
      <p className="text-muted">It typically shows some important attribute or status.</p>
      <Badge>12 Tage ab 27.5.2021</Badge>
      <p className="text-muted mt-2">
        It can also be a <code>{'<a>'}</code>.
      </p>
      <Badge as="a" href={'#example-link'}>
        23 Tage ab 27.5.2021
      </Badge>
      <p className="text-muted mt-2">
        It can also be a <code>{'<button>'}</code>.
      </p>
      <Badge as="button" onClick={action('badge click')}>
        42 Tage ab 27.5.2021
      </Badge>
    </div>
  )
}

export const themeColors = () => {
  return (
    <div>
      <h1>Badge</h1>
      <p className="text-muted">
        Use the <code>colorClassName</code> prop to override the components default color classes (
        <code>bg-secondary text-dark</code>).
      </p>
      <p className="text-muted">
        Example with <code>bg-danger</code>
      </p>
      <Badge colorClassName="bg-danger text-light">12 Tage ab 27.5.2021</Badge>
      <p></p>
      <p className="text-muted">
        Example with <code>bg-warning text-dark</code>
      </p>
      <Badge colorClassName="bg-warning text-dark">12 Tage ab 27.5.2021</Badge>
      <p></p>
      <p className="text-muted">
        Note that combination with a light background color you will have to manually add <code>.text-dark</code>{' '}
        (Bootstrap does not automatically adjust it as for instance with buttons)
      </p>
      <p className="text-muted">
        Do not use the ordinary <code>className</code> prop to modify colors unless you want to fight with CSS
        specifity.
      </p>
    </div>
  )
}
