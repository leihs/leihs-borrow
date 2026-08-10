# Postcss 8.5.24 BOM issue

To see the problem install postcss 8.5.24 or higher and run:

`npm run build && LC_ALL=C grep -n $'\xEF\xBB\xBF' dist/borrow-ui.css`

There will be a BOM (byte order mark) in the middle of the build output (which will break the styles in the browser).

Before commit https://github.com/postcss/postcss/commit/9a114f62b0deb37be859102f93b414b49385805a postcss stripped all BOMS.

The BOM seems to come from Bootstrap, because it goes away when outcommenting `@import '~bootstrap/scss/bootstrap';` in `styles.scss`, but there is no BOM in the Bootstrap dist files (checked with `LC_ALL=C grep -rn $'\xEF\xBB\xBF' .`)

The solution is to pin postcss on version 8.5.23.
