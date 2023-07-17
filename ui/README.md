# Borrow UI

Shared UI components (React), theme (based on Bootstrap) and styleguide (Storybook) for Borrow app.

## Stack

- React 17
- Bootstrap 5
- Storybook 7
- Webpack 5
- Babel
- Prettier
- ESLint

## Artifact output paths

- `dist/borrow-ui.js`: Component library
- `dist/borrow-ui.css`: Theme (styles)
- `dist/**.woff`, `dist/**.woff2`: Fonts

## Guide

### Basics

Modes of development:

- Start Storybook (`npm run storybook`)
- Run Borrow app in watch mode (instructions see ../README.md) and start UI library in watch mode (`npm run watch`)  
  Changes in components will automatically reflect in Borrow app. Changes in SCSS require a browser reload. 
- (or both together)

Note that Storybook has its own build chain and is not affected by `webpack.config.js`. 

If there is a problem with the library build while everything is fine in Storybook: use `test-app` to debug in a standalone/runnable environment  (see below). 

### Storybook

- `npm run storybook`: Start storybook (http://localhost:6006)
- `npm run build-storybook`: Build deployable (goes to `storybook-static` folder)
- `npm run test-storybook`: Run a smoke test on all stories

### Lint and format

Note that currently only Prettier's rule definitions are configured in ESLint. 

- `npm run lint`: Lint all files
- `npm run prettier`: Autoformat all files

### Library development and build

- `npm run watch`: Start theme and lib in watch/dev mode (use along with watch mode in Borrow app)
- `npm run build`: Build theme and lib for production
- `npm run start`: Start dev server for `test-app` (http://localhost:8081/test-app.html). Enable `test-app` in `webpack.config.js` for this to work
