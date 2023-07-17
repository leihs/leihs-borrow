import React from 'react'
import InfoMessage from './InfoMessage'
import Warning from './Warning'

export default {
  title: 'Design Components/Content/Warning & Info',
  component: Warning
}

export const warning = () => {
  return (
    <div>
      <h1>Warning</h1>
      <p className="text-muted">Show a warning message in whatever context (not only forms).</p>
      <p className="text-muted">
        It ist based on Bootstrap&apos;s invalid feedback for forms, but does not depend on the presence of an invalid
        form element.
      </p>
      <p className="text-muted">There is also an icon added so the message is not discernible by its color only.</p>
      <Warning>1 Gegenstand ungültig</Warning>
    </div>
  )
}
warning.storyName = 'Warning'

export const infoMessage = () => {
  return (
    <div>
      <h1>InfoMessage</h1>
      <p className="text-muted">
        Marks non-critical information (hints, annotations, instructions, friendly reminders etc.).
      </p>
      <p className="text-muted">
        Same behaviour as <code>Warning</code>, but in gray and with info icon.
      </p>
      <InfoMessage>Zeitangabe in Sekunden</InfoMessage>
    </div>
  )
}
infoMessage.storyName = 'InfoMessage'

export const wrap = () => {
  return (
    <div>
      <h1>Warning &amp; Info</h1>
      <p className="text-muted">Indented wrapping when the link text is too large:</p>
      <div style={{ maxWidth: '40rem' }}>
        <Warning>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Dicta officiis quod esse dolores quia perferendis
          minima architecto. Nobis itaque, unde quibusdam voluptatibus aut accusantium ipsa, facere autem deserunt,
          praesentium perspiciatis?
        </Warning>
        <InfoMessage>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Dicta officiis quod esse dolores quia perferendis
          minima architecto. Nobis itaque, unde quibusdam voluptatibus aut accusantium ipsa, facere autem deserunt,
          praesentium perspiciatis?
        </InfoMessage>
      </div>
    </div>
  )
}
export const restProps = () => {
  return (
    <div>
      <h1>Warning &amp; Info</h1>
      <p className="text-muted">Set arbitrary attributes with restProps</p>
      <Warning className="border border-primary mb-3" style={{ fontSize: '1rem' }}>
        1 Gegenstand ungültig
      </Warning>
      <InfoMessage className="border border-primary" style={{ fontSize: '1rem' }}>
        Zeitangabe in Sekunden
      </InfoMessage>
    </div>
  )
}
