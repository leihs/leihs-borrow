import '../src/scss/styles.scss'
import './fake-time'

/** @type { import('@storybook/react').Preview } */
const preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/
      }
    },
    options: {
      storySort: {
        order: ['Overview', 'Theme', 'Design Components', 'Feature Components', 'Prototypes']
      }
    }
  }
}

export default preview
