/** @type { import('@storybook/react-webpack5').StorybookConfig } */
const config = {
  stories: ['../src/**/*.stories.@(js|jsx|ts|tsx)'],
  addons: ['@storybook/addon-links', '@storybook/addon-essentials', '@storybook/addon-interactions'],
  framework: {
    name: '@storybook/react-webpack5',
    options: {
      legacyRootApi: true
    }
  },
  core: {
    enableCrashReports: false
  },
  docs: {
    autodocs: 'tag'
  },
  staticDirs: ['../static'],
  webpackFinal: async (config, { configType }) => {
    // Import SVG as React component instead as static asset
    const filesRule = config.module.rules.find(r => r.test.test('.svg'))
    filesRule.exclude = /\.svg$/
    config.module.rules.push({
      test: /\.svg$/,
      use: ['@svgr/webpack']
    })

    // SCSS support
    config.module.rules.push({
      test: /\.(scss)$/,
      use: [
        'style-loader',
        'css-loader',
        'postcss-loader',
        {
          loader: 'sass-loader',
          options: {
            sassOptions: {
              quietDeps: true,
              silenceDeprecations: ['import', 'global-builtin']
            }
          }
        }
      ]
    })

    // Ensure babel-loader transforms JSX in story files
    config.module.rules.push({
      test: /\.(js|jsx)$/,
      exclude: /node_modules/,
      use: ['babel-loader']
    })

    // Storybook bundles are large by nature — suppress size warnings
    config.performance = { hints: false }

    // Suppress React 18 hook warnings caused by storybook internals running against React 17
    config.ignoreWarnings = [/export 'useInsertionEffect'/]

    return config
  }
}
export default config
