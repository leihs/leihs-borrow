//const HtmlWebpackPlugin = require('html-webpack-plugin')
const miniCssExtractPlugin = require('mini-css-extract-plugin')

const baseConfig = {
  output: {
    filename: '[name].js',
    libraryTarget: 'umd'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader'
        }
      },
      {
        test: /\.(scss)$/,
        use: [
          {
            loader: miniCssExtractPlugin.loader
          },
          {
            loader: 'css-loader'
          },
          {
            loader: 'postcss-loader',
            options: {
              postcssOptions: {
                plugins: () => [require('autoprefixer')]
              }
            }
          },
          {
            loader: 'sass-loader'
          }
        ]
      },
      {
        test: /\.(png|jp(e*)g|gif)$/,
        type: 'asset/resource'
      },
      {
        test: /\.svg$/,
        use: ['@svgr/webpack']
      }
    ]
  },
  plugins: [new miniCssExtractPlugin()],
  performance: {
    maxEntrypointSize: 1000000,
    maxAssetSize: 1000000
  }
}

module.exports = [
  // Components and theme build
  {
    ...baseConfig,
    entry: {
      'borrow-ui': './src/borrow-ui.js'
    },
    externals: {
      react: {
        root: 'React',
        commonjs2: 'react',
        commonjs: 'react',
        amd: 'react'
      },
      'react-dom': {
        root: 'ReactDOM',
        commonjs2: 'react-dom',
        commonjs: 'react-dom',
        amd: 'react-dom'
      }
    }
  }
  // Build "test app" (uncomment to debug the build with a standalone runnable app)
  /* {
    ...baseConfig,
    entry: {
      'test-app': './src/test-app.js'
    },
    plugins: [
      ...baseConfig.plugins,
      new HtmlWebpackPlugin({
        template: './src/test-app.html',
        filename: './test-app.html'
      })
    ]
  } */
]
