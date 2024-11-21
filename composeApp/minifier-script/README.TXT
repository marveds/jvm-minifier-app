// Webpack bundle

rm -rf dist node_modules package-lock.json
npm install
npx webpack --config webpack.config.js
pkg --targets node18-linux-x64,node18-macos-x64,node18-win-x64 dist/bundle.js
// Testing
./bundle-macos /Users/marveds/Documents/www_html/turflogistics-yii2/web/js/test.js

// ncc bundle

npm install --save-dev @vercel/ncc
npx @vercel/ncc build main.js -o dist

to create exdcutable script add to top of file : #!/usr/bin/env node
chmod +x dist/minifier.js


// Find and stop the process:
ps -ax | grep minifier
kill -9 <pid>
