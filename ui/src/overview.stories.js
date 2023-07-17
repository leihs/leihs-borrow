import React from 'react'

export default {
  title: 'Overview',
  parameters: { viewport: { defaultViewport: 'reset' } }
}

const drawling = `┌───────────────────────────────────────────────────────────────────┐
│                                                                   │
│ Borrow App (CLJS)                                                 │
│                                                                   │
└────────────┬────────┬───────────────┬─────────────────────────────┘
             │        │               │                              
┌────────────┼────────┼───────────────┼─────────────────────────────┐
│            │        │               │                             │
│ Borrow     │        │           ┌───▼───────────────────────┐     │
│   UI       │        │           │    Feature Components     │     │
│            │        │           └───────┬──────────────┬────┘     │
│            │        │                   │              │          │
│            │    ┌───▼───────────────────▼──────────┐   │          │
│            │    │        Design Components         │   │          │
│            │    └───────────────┬──────────────────┘   │          │
│            │                    │                      │          │
│     ┌──────▼────────────────────▼──────────────────────▼────┐     │
│     │                    Bootstrap Theme                    │     │
│     └───────────────────────────────────────────────────────┘     │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘`

export function Overview() {
  return (
    <div>
      <h1>UI components for Borrow App</h1>
      <p>A set of design components on top of a Bootstrap theme.</p>
      <pre style={{ fontSize: '0.8rem', fontFamily: 'Monaco, Monospace' }}>{drawling}</pre>
      <h2>Story breakdown</h2>
      <ul className="list-group">
        <li className="list-group-item">
          <h2>Theme</h2>
          <div>Reference about colors, spacing, typography etc.</div>
        </li>
        <li className="list-group-item">
          <h2>Design Components</h2>
          <div>Detailed stories about all available design components.</div>
        </li>
        <li className="list-group-item">
          <h2>Feature Components</h2>
          <div>
            Describing the few available feature components (why &quot;few&quot;? because features are preferably
            composed in Borrow App).
          </div>
        </li>
        <li className="list-group-item">
          <h2>Prototypes</h2>
          <div>Conceptual drafts for the feature composition in Borrow App.</div>
        </li>
      </ul>
    </div>
  )
}
