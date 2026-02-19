# Security Vulnerability Fixes

**Date:** 2026-02-19
**Branch:** `mr/pr/lib-update`

## Summary

| Status | Count |
|--------|-------|
| Fixed | 12 |
| Cannot fix (no patch available) | 3 |
| Requires major migration | 4 |

---

## Fixed Vulnerabilities

### npm — `ui/package-lock.json`

#### 1. node-tar (4 CVEs) — High

| Issue | GitHub Alert |
|-------|-------------|
| Race Condition via Unicode Ligature Collisions on macOS APFS | #277 |
| Arbitrary File Creation/Overwrite via Hardlink Path Traversal | #279 |
| Arbitrary File Overwrite and Symlink Poisoning via Insufficient Path Sanitization | #276 |
| Arbitrary File Read/Write via Hardlink Target Escape Through Symlink Chain | #289 |

- **Before:** `tar@6.2.1`
- **After:** `tar@7.5.9` (via npm override)
- **Fix:** Added `"tar": ">=7.4.3"` to `overrides` in `ui/package.json`

#### 2. qs (2 CVEs) — High / Low

| Issue | GitHub Alert |
|-------|-------------|
| arrayLimit bypass in bracket notation allows DoS via memory exhaustion | #274 |
| arrayLimit bypass in comma parsing allows denial of service | #285 |

- **Before:** `qs@6.13.0`
- **After:** `qs@6.15.0` (via npm override)
- **Fix:** Added `"qs": ">=6.14.0"` to `overrides` in `ui/package.json`

#### 3. Lodash Prototype Pollution — Moderate

- **Alert:** #278 — Prototype Pollution in `_.unset` and `_.omit`
- **Before:** `lodash@4.17.21`
- **After:** `lodash@4.17.23` (via npm override)
- **Fix:** Added `"lodash": ">=4.17.23"` to `overrides` in `ui/package.json`

#### 4. webpack-dev-server (2 CVEs) — Moderate

| Issue | GitHub Alert |
|-------|-------------|
| Source code theft via malicious site (non-Chromium) | #237 |
| Source code theft via malicious site | #236 |

- **Before:** `webpack-dev-server@4.15.2`
- **After:** `webpack-dev-server@5.2.3` (direct upgrade)
- **Fix:** Bumped `"webpack-dev-server"` from `"^4.15.1"` to `"^5.2.0"` in `ui/package.json`

#### 5. webpack (2 CVEs) — Low

| Issue | GitHub Alert |
|-------|-------------|
| buildHttp allowedUris bypass via URL userinfo (@) leading to SSRF | #282 |
| buildHttp HttpUriPlugin allowedUris bypass via HTTP redirects | #281 |

- **Before:** `webpack@5.97.1`
- **After:** `webpack@5.105.2` (direct upgrade)
- **Fix:** Bumped `"webpack"` from `"^5.88.1"` to `"^5.105.0"` in `ui/package.json`

### RubyGems — `Gemfile.lock`

#### 6. Rack (2 CVEs) — High / Moderate

| Issue | GitHub Alert |
|-------|-------------|
| Directory Traversal via `Rack::Directory` | #286 |
| Stored XSS via javascript: filenames in `Rack::Directory` | #288 |

- **Before:** `rack 3.1.19`
- **After:** `rack 3.1.20`
- **Fix:** `bundle update rack`

#### 7. Faraday SSRF — Moderate

- **Alert:** #284 — SSRF via protocol-relative URL host override in `build_exclusive_url`
- **Before:** `faraday 2.7.2`
- **After:** `faraday 2.14.1`
- **Fix:** `bundle update faraday`

#### 8. Sinatra ReDoS — Low

- **Alert:** #263 — ReDoS through ETag header value generation
- **Before:** `sinatra 4.1.1`
- **After:** `sinatra 4.2.1`
- **Fix:** `bundle update sinatra`

---

## Cannot Fix — No Patch Available

#### 9. Elliptic — Low (Development only)

- **Alert:** #275 — Uses a Cryptographic Primitive with a Risky Implementation
- **Package:** `elliptic@6.6.1` in `package-lock.json`
- **Status:** 6.6.1 is the latest published version. No fix available upstream.
- **Risk:** Low severity, development dependency only (transitive via `shadow-cljs` -> `crypto-browserify`). Does not affect production.

#### 10. ajv ReDoS — Moderate

- **Alert:** #287 — ReDoS when using `$data` option
- **Package:** `ajv@6.12.6` in `ui/package-lock.json`
- **Status:** No patched 6.x version exists. The fix is in ajv 8.x, but `eslint` and `schema-utils` require ajv ^6. Overriding to 8.x would break the eslint toolchain.
- **Risk:** Moderate severity, development dependency only. Only exploitable if `$data` option is used with untrusted schemas.

#### 11. esbuild Dev Server — Moderate (Development only)

- **Alert:** #219 — Any website can send requests to the development server and read the response
- **Package:** `esbuild@0.18.20` in `ui/package-lock.json`
- **Status:** Transitive dependency of Storybook 7.x. Fix requires Storybook 8 migration (see below).
- **Risk:** Moderate severity, development dependency only. Only affects local dev server.

---

## Requires Major Migration — Storybook 7 → 8

#### 12. Storybook Environment Variable Exposure — High (Development only)

- **Alert:** #273 — Manager bundle may expose environment variables during build
- **Package:** `storybook@7.6.20` and all `@storybook/*` packages in `ui/package-lock.json`
- **Status:** Fix requires upgrading from Storybook 7 to Storybook 8 (major version).

Upgrading Storybook to v8 would also resolve:
- esbuild vulnerability (#219)
- Additional transitive dependency issues (minimatch ReDoS)

### Migration steps for Storybook 8

1. Update all `@storybook/*` packages to `^8.5.0`
2. Remove `@storybook/addon-styling` (deprecated in v8, CSS/Sass support is built-in)
3. Add `@storybook/addon-webpack5-compiler-babel` (new in v8)
4. Update `.storybook/main.js` — the `addon-styling` sass config moves to `webpackFinal`
5. Update `.storybook/preview.js` — `argTypesRegex` is deprecated, remove or replace with explicit `fn()` actions
6. Run `npx storybook@latest upgrade` for automated migration assistance
7. See: https://storybook.js.org/docs/migration-guide

---

## Files Changed

| File | Changes |
|------|---------|
| `ui/package.json` | Bumped `webpack` to `^5.105.0`, `webpack-dev-server` to `^5.2.0`; added overrides for `tar`, `qs`, `lodash` |
| `ui/package-lock.json` | Regenerated |
| `Gemfile.lock` | Updated `rack`, `faraday`, `sinatra` (and transitive deps) |
