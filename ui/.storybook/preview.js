import '../src/scss/styles.scss'
import './fake-time'

/** @type { import('@storybook/react').Preview } */
const preview = {
  parameters: {
    actions: { argTypesRegex: '^on[A-Z].*' },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/
      }
    },
    options: {
      storySort: {
        order: ['Theme', 'Design Components', 'Feature Components', 'Prototypes']
      }
    }
  }
}

export default preview
