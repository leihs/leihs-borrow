import IntlMessageFormat from 'intl-messageformat'

// NOTE: langs other than English and German are not supported yet, always fall back to English
const FALLBACK_LOCALE = 'en-GB'

// NOTE: this should only be used in stories and some components where needed.
// Those components are only handed simple key/value pairs.
// when in doubt, use the translation function of the App instead and give preformatted strings to the UI!
export const translate = (translations, key, locales, values) => {
  locales = (typeof locales === 'string' ? [locales] : locales).concat(FALLBACK_LOCALE)
  let strings, string

  if (!locales || !locales.length || !locales[0]) throw new Error(`Missing locales, got: '${locales}'!`)

  try {
    strings = translations[key]
    if (!strings) throw Error()
  } catch {
    return `{{ missing: ${key} }}`
  }

  try {
    string = locales.map(locale => strings[locale]).filter(Boolean)[0]
    if (!string) throw Error()
  } catch {
    return `{{ missing: ${key} }}`
  }

  try {
    return new IntlMessageFormat(string, locales).format(values)
  } catch (err) {
    // eslint-disable-next-line no-console
    console.warn('translation error!', err)
    return `{{ failing: ${key} }}`
  }
}
