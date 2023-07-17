import React from 'react'
import { translate } from '../lib/translate'

export default {
  title: 'Lib/Translations',
  parameters: { viewport: { defaultViewport: 'reset' } }
}

// NOTE: shortcut for the examples, they are always in just 1 language and key is same as string
const t = (key, locale, values) => translate({ [key]: { [locale]: key } }, key, locale, values)

const makeExample = (string, locales, valuesList = [undefined]) => (
  <ul className="list-group mb-4">
    <li className="list-group-item">
      <div className="mb-1 preserve-linebreaks">
        <p className="d-block fs-3 font-monospace preserve-linebreaks">
          <span className="text-muted">input: </span>
          {string}
        </p>
      </div>
    </li>
    {locales.map(locale =>
      valuesList.map((values, i) => (
        <li key={i} className="list-group-item">
          {!!values && (
            <span className="fs-5 d-block text-muted font-monospace">
              values: {JSON.stringify(values) || String(values)}
            </span>
          )}
          {<span className="fs-5 d-block text-muted font-monospace">locale: {locale}</span>}
          <p className="mb-0 fs-2">
            <span className="fs-3 text-muted font-monospace">result: </span>
            {t(string, locale, values)}
          </p>
        </li>
      ))
    )}
  </ul>
)

export const translations = () => (
  <div className="p-2">
    <h1>Translation and Localization</h1>

    <h2>Translation Function / Library</h2>

    <p>
      More Docs: We use the{' '}
      <a className="link-primary" href="https://formatjs.io/docs/core-concepts/icu-syntax">
        ICU Message syntax
      </a>{' '}
      and the{' '}
      <a className="link-primary" href="https://formatjs.io/docs/intl-messageformat/#how-it-works">
        <code>IntlMessageFormat</code>
      </a>{' '}
      library from{' '}
      <a className="link-primary" href="https://formatjs.io/">
        Format.js
      </a>
      .
    </p>

    <h2>Examples</h2>

    <h3>Simple Text</h3>
    {makeExample('Hello, World!', ['de-CH'])}

    <h3>With Placeholders/Variables</h3>
    {makeExample('Hello {location}, my name is {name}!', ['de-CH'], [{ location: 'World', name: 'Normin' }])}

    <h3>With Plurals</h3>
    {makeExample(
      `You have {numItems, plural,
            =0 {no items.}
            =1 {one item.}
            other {# items.}}`,
      ['de-CH'],
      [{ numItems: 0 }, { numItems: 1 }, { numItems: 42 }]
    )}

    <h3>Number formatting</h3>
    {makeExample('Die Anzahl ist: {count, number}!', ['en-GB', 'de-CH'], [{ count: 10000 }])}

    <h3>Date formatting</h3>
    {makeExample(
      'Local Date: {startDate, date}!',
      ['en-GB', 'en-US', 'de-CH'],
      [{ startDate: Date.parse('2037-11-09') }]
    )}

    <h3 className="mb-0">Edge Cases</h3>
    <small className="mt-2 mb-4">(only output is shown, see story source for details)</small>

    <h4 className="mt-2">Missing Translations</h4>
    <p>{translate({ foo: { 'de-CH': 'Fuh', 'de-FR': 'fou' } }, 'not-foo', 'de-CH')}</p>

    <h4 className="mt-2">Fallback Languages</h4>
    <p>
      {translate({ foo: { 'de-CH': 'Fuh', 'de-FR': 'fou' } }, 'foo', ['en-GB', 'de-CH', 'de-FR'])} /{' '}
      {translate({ foo: { 'de-CH': 'Fuh', 'de-FR': 'fou' } }, 'foo', ['en-GB', 'de-FR'])}
    </p>
  </div>
)
