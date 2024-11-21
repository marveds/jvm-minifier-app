const path = require('path');
// const webpack = require('webpack');
// const nodeExternals = require('webpack-node-externals');

module.exports = {
  entry: './main.js',
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'minifier.js',
  },
  target: 'node', // Ensures compatibility with Node.js environment
  // externals: [nodeExternals()], // Excludes all modules in node_modules
  mode: 'production',
  resolve: {
    alias: {
      less: path.resolve(__dirname, 'node_modules/less')
    }
  }
};


// // webpack.config.js
    // const path = require('path');
    // const nodeExternals = require('webpack-node-externals');
    // const webpack = require('webpack');

    // // List of dependencies to include in the bundle
    // const includeModules = [
    // '@babel/core',
    // '@babel/preset-env',
    // '@babel/preset-typescript',
    // '@swc/core',
    // 'babel-loader',
    // 'coffee-loader',
    // 'coffee-script',
    // 'css-loader',
    // 'css-minimizer-webpack-plugin',
    // 'esbuild',
    // 'less',
    // 'less-loader',
    // 'mini-css-extract-plugin',
    // 'path-browserify',
    // 'sass',
    // 'sass-loader',
    // 'style-loader',
    // 'stylus',
    // 'stylus-loader',
    // 'terser-webpack-plugin',
    // 'ts-loader',
    // 'typescript',
    // 'uglify-js',
    // // 'webpack',
    // // 'webpack-cli',
    // // 'webpack-remove-empty-scripts',
    // ];

    // module.exports = {
    //     entry: './main.js',
    //     output: {
    //         path: path.resolve(__dirname, 'dist'),
    //         filename: 'bundle.js',
    //     },
    //     mode: 'production',
    //     target: 'node',
    //     externals: [
    //         nodeExternals({
    //             allowlist: includeModules,
    //             additionalModuleDirs: ['node_modules'],
    //         }),
    //     ],
    //     module: {
    //         rules: [
    //         {
    //             test: /\.d\.ts$/,
    //             use: 'ignore-loader',
    //         },
    //         {
    //             test: /\.m?js$/,
    //             exclude: /node_modules/,
    //             use: 'babel-loader',
    //         },
    //         // ... other loaders if necessary
    //         ],
    //     },
    //     resolve: {
    //         extensions: ['.js', '.json'],
    //         alias: {
    //         '@swc/wasm': false,
    //         'esbuild': false,
    //         },
    //     },
    //     plugins: [
    //         new webpack.IgnorePlugin({
    //         resourceRegExp: /^@swc\/wasm$/,
    //         }),
    //     ],
    // };
