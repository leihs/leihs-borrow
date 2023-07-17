import React from 'react'

export default {
  title: 'Theme/Typography'
}

const lorem =
  'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore'

const exampleTextWithLinebreaks = `Use the class .preserve-linebreaks to
preserve linebreaks,
e.g. as they were entered
by a user into
an textarea.

Even more text after a break.
`

export function typography() {
  return (
    <div className="pb-5">
      <h1>Typography</h1>
      <p className="text-muted">Headings (h1, h2 and h3):</p>
      <h1>Heading 1</h1>
      <h2>Heading 2</h2>
      <h3>Heading 3</h3>

      <p className="text-muted">Font sizes (default, .small, .very-small):</p>
      <p>
        <span>Default size: {lorem}</span>
      </p>
      <p>
        <span className="small">Small: {lorem}</span>
      </p>
      <p>
        <span className="very-small">Very small: {lorem}</span>
      </p>
      <p className="text-muted">Note that .very-small is a custom extension, not in the Bootstrap standard.</p>

      <p className="text-muted">Bold font (.fw-bold):</p>
      <p>
        <span className="fw-bold">Default size: {lorem}</span>
      </p>
      <p>
        <span className="small fw-bold">Small: {lorem}</span>
      </p>
      <p>
        <span className="very-small fw-bold">Very small: {lorem}</span>
      </p>
      <p className="text-muted">Bold font is used for important content (but not for titles)</p>

      <p className="text-muted">Utils:</p>
      <code>.preserve-linebreaks</code>
      <p className="preserve-linebreaks">{exampleTextWithLinebreaks}</p>
      <code>.decorate-links</code>
      <p className="decorate-links">
        Some content with a <a href=".">link</a> inline.
      </p>
      <a href="." className="decorate-links">
        Standalone link
      </a>
    </div>
  )
}
