# prepare:
cd leihs-ui
test -d node_modules && npm i || { npm ci || npm i ;}

# those can run in parallel!
npm run build:theme || exit 1
npm run build:ssr || exit 1

cd -
