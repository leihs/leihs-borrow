# prepare:
cd leihs-ui
test -d node_modules && npm i || { npm ci || npm i ;}

# those can run in parallel!
npm run build:theme
npm run build:ssr

cd -
