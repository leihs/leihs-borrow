import React from 'react'
import Let from './Let'

export default {
  title: 'Lib/LetComponent',
  compont: Let
}

export function letComponent() {
  return (
    <>
      <h1>`Let` component</h1>
      <p className="text-muted">Fragment with a local scope</p>
      <Let a={1} b={2}>
        {({ a, b }) => (
          <div>
            <div>a = {a}</div>
            <div>b = {b}</div>
            <div>a + b = {a + b}</div>
          </div>
        )}
      </Let>
    </>
  )
}
