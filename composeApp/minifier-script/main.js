const chokidar = require('chokidar');
const path = require('path');
const babel = require('@babel/core');
const fs = require('fs');
const os = require('os');
const ts = require('typescript');
const less = require('less').default || require('less');
const sass = require('sass');
const stylus = require('stylus');
const userDataPath = os.homedir();
const minifierDataPath = path.join(userDataPath, 'MinifierData', 'minifierData.json');

let watchers = [];

function handleFileChange(filePath) {
    try {
        if (filePath.endsWith('.min.js') || filePath.endsWith('.min.css')) {
            console.log('debug-message', `Ignoring minified file: ${filePath}`);
            return;
        }

        if (filePath.endsWith('.js')) {
            console.log('watch-update', `Processing JavaScript file: ${filePath}`);
            processJavaScriptFile(filePath);
        } else if (filePath.endsWith('.less')) {
            console.log('watch-update', `Processing LESS file: ${filePath}`);
            processFile(filePath, {
                inputExtension: '.less',
                outputExtension: '.css',
                type: 'LESS',
            });
        } else if (filePath.endsWith('.scss') || filePath.endsWith('.sass')) {
            console.log('watch-update', `Processing SCSS file: ${filePath}`);
            processFile(filePath, {
                inputExtension: path.extname(filePath),
                outputExtension: '.css',
                type: 'SCSS',
            });
        } else if (filePath.endsWith('.styl')) {
            console.log('watch-update', `Processing Stylus file: ${filePath}`);
            processFile(filePath, {
                inputExtension: '.styl',
                outputExtension: '.css',
                type: 'Stylus',
            });
        } else if (filePath.endsWith('.ts')) {
            console.log('watch-update', `Processing TypeScript file: ${filePath}`);
            processTypeScriptFile(filePath);
        } else {
            console.error('watch-error', `Unsupported file type: ${filePath}`);
        }
    } catch (error) {
        console.error('watch-error', `Error processing file: ${error}`);
    }
}

function processJavaScriptFile(filePath) {
    try {
        const presetEnv = require('@babel/preset-env');

        const inputCode = fs.readFileSync(filePath, 'utf8');
        const output = babel.transformSync(inputCode, {
            presets: [presetEnv],
            sourceMaps: false,
            minified: true,
            comments: false,
        });
        const outputFilePath = filePath.replace(/\.js$/, '.min.js');
        fs.writeFileSync(outputFilePath, output.code, 'utf8');
        console.log('watch-update', `JavaScript processing completed: ${outputFilePath}`);
    } catch (error) {
        console.error('watch-error', `Error processing JavaScript file: ${error}`);
    }
}

function processTypeScriptFile(filePath) {
    try {
        const inputCode = fs.readFileSync(filePath, 'utf8');
        const output = ts.transpileModule(inputCode, {
        compilerOptions: {
            module: ts.ModuleKind.CommonJS,
            target: ts.ScriptTarget.ES5,
            sourceMap: false,
            removeComments: true,
        },
        });
        const outputFilePath = filePath.replace(/\.ts$/, '.min.js');
        fs.writeFileSync(outputFilePath, output.outputText, 'utf8');
        console.log('watch-update', `TypeScript processing completed: ${outputFilePath}`);
    } catch (error) {
        console.error('watch-error', `Error processing TypeScript file: ${error}`);
    }
}

async function processFile(filePath, options) {
    try {
        const { inputExtension, outputExtension, type } = options;
        const baseName = path.basename(filePath, inputExtension);
        const outputFilePath = path.join(path.dirname(filePath), `${baseName}${outputExtension}`);
        const inputContent = fs.readFileSync(filePath, 'utf8');

        if (type === 'LESS') {
            // Process LESS file
            less.render(inputContent, { filename: filePath })
                .then(output => {
                    fs.writeFileSync(outputFilePath, output.css, 'utf8');
                    console.log('watch-update', `${type} processing completed: ${outputFilePath}`);
                })
                .catch(error => {
                    console.error('watch-error', `Error processing ${type}: ${error}`);
                });
        } else if (type === 'SCSS') {
            // Process SCSS/SASS file
            const result = sass.renderSync({
                file: filePath,
                outFile: outputFilePath,
                outputStyle: 'compressed', // Minify the output
            });
            fs.writeFileSync(outputFilePath, result.css, 'utf8');
            console.log('watch-update', `${type} processing completed: ${outputFilePath}`);
        } else if (type === 'Stylus') {
            // Process Stylus file
            stylus.render(inputContent, { filename: filePath, compress: true }, (err, css) => {
                if (err) {
                    console.error('watch-error', `Error processing ${type}: ${err}`);
                } else {
                fs.writeFileSync(outputFilePath, css, 'utf8');
                    console.log('watch-update', `${type} processing completed: ${outputFilePath}`);
                }
        });
        } else {
            console.error('watch-error', `Unsupported file type: ${type}`);
        }
    } catch (error) {
        console.error('watch-error', `Error processing file: ${error}`);
    }
}

function loadMinifierData(callback) {
	try {
        // check if the file exists
        console.log('minifierDataPath', minifierDataPath);
        if (!fs.existsSync(minifierDataPath)) {
            console.error('Error loading watched paths:', 'minifierData.json does not exist');
            callback({ paths: [], isWatching: false, notifictionState: true });
            return;
        }

		fs.readFile(minifierDataPath, 'utf8', (err, data) => {
			if (err) {
				if (err.code !== 'ENOENT') {
					console.error('Error reading watched paths:', err);
				}
				callback({ paths: [], isWatching: false, notifictionState: true });
			} else {
				try {
					const parsedData = JSON.parse(data);
					callback(parsedData);
				} catch (parseErr) {
					console.error('Error parsing watched paths:', parseErr);
					callback({ paths: [], isWatching: false, notifictionState: true });
				}
			}
		});
	} catch (error) {
		console.error('watch-error', `Error loading watched paths: ${error}`);
	}
}

function startWatchingFolders(folders) {
	try {
		const uniqueFolders = [...new Set(folders)];

		uniqueFolders.forEach((folder) => {
			if (!fs.existsSync(folder)) {
				console.error('watch-error', `Folder does not exist: ${folder}`);
				return;
			}

			if (watchers[folder]) {
				console.log(`Already watching folder: ${folder}`);
				return;
			}

			console.log('debug-message', `Starting watcher for folder: ${folder}`);

			const watcher = chokidar.watch(folder, {
				ignored: /(^|[/\\])(\..*|node_modules|lib|assets|cometchat)/,
				persistent: true,
				ignoreInitial: true,
			});

			watcher
				.on('add', (filePath) => handleFileChange(filePath))
				.on('change', (filePath) => handleFileChange(filePath))
				.on('unlink', (filePath) => {
					console.log('watch-update', `File removed: ${filePath}`);
				});
			watchers.push(watcher);

			console.log('set-is-watching', true);
		});
	} catch (error) {
		console.error('watch-error', `Error starting watcher: ${error}`);
	}
}

function loadFromMinifierData() {
    let folderArray = [];

    loadMinifierData((savedData) => {
        folderArray = savedData.folders;
        startWatchingFolders(folderArray);
    });
}

loadFromMinifierData();

// handleFileChange(filePath);





// const path = require('path');
// const fs = require('fs');
// const webpack = require('webpack');
// const MiniCssExtractPlugin = require('mini-css-extract-plugin');
// const RemoveEmptyScriptsPlugin = require('webpack-remove-empty-scripts');
// const TerserPlugin = require('terser-webpack-plugin');

// const filePath = process.argv[2];

// function handleFileChange(filePath) {
// 	try {
// 		if (filePath.endsWith('.min.js') || filePath.endsWith('.min.css')) {
// 			console.log('debug-message', `Ignoring minified file: ${filePath}`);
// 			return;
// 		}

// 		if (filePath.endsWith('.js')) {
// 			console.log('watch-update', `Processing JavaScript file: ${filePath}`);
// 			processJavaScriptFile(filePath);
// 		} else if (filePath.endsWith('.less')) {
// 			console.log('watch-update', `Processing LESS file: ${filePath}`);
// 			processFile(filePath, {
// 				inputExtension: '.less',
// 				outputExtension: '.css',
// 				testRegex: /\.less$/,
// 				loaders: [
// 					process.env.NODE_ENV !== 'production' ? 'style-loader' : MiniCssExtractPlugin.loader,
//                     {
//                         loader: 'css-loader',
//                         options: { url: false, esModule: false, }
//                     },
//                     'less-loader',
// 				],
// 				type: 'LESS',
// 			});
// 		} else if (filePath.endsWith('.scss') || filePath.endsWith('.sass')) {
// 			console.log('watch-update', `Processing SCSS file: ${filePath}`);
// 			processFile(filePath, {
// 				inputExtension: path.extname(filePath),
// 				outputExtension: '.css',
// 				testRegex: /\.(scss|sass)$/,
// 				loaders: [
// 					{
// 						loader: MiniCssExtractPlugin.loader,
// 						options: {
// 							esModule: false,
// 						},
// 					},
// 					'css-loader',
// 					'sass-loader',
// 				],
// 				type: 'SCSS',
// 			});
// 		} else if (filePath.endsWith('.styl')) {
// 			console.log('watch-update', `Processing Stylus file: ${filePath}`);
// 			processFile(filePath, {
// 				inputExtension: '.styl',
// 				outputExtension: '.css',
// 				testRegex: /\.styl$/,
// 				loaders: [
// 					{
// 						loader: MiniCssExtractPlugin.loader,
// 						options: {
// 							esModule: false,
// 						},
// 					},
// 					'css-loader',
// 					'stylus-loader',
// 				],
// 				type: 'Stylus',
// 			});
// 		} else if (filePath.endsWith('.ts')) {
// 			console.log('watch-update', `Processing TypeScript file: ${filePath}`);
// 			processJavaScriptFile(filePath, {
// 				inputExtension: '.ts',
// 				outputExtension: '.min.js',
// 				testRegex: /\.ts$/,
// 				loaders: [
// 					{
// 						loader: 'babel-loader',
// 						options: {
// 							presets: ['@babel/preset-env', '@babel/preset-typescript'],
// 						},
// 					},
// 				],
// 				type: 'TypeScript',
// 			});
// 		}
// 	} catch (error) {
// 		console.error('watch-error', `Error processing file: ${error}`);
// 	}
// }

// function processFile(filePath, options) {
// 	try {
// 		const { inputExtension, outputExtension, testRegex, loaders, type } = options;
// 		const baseName = path.basename(filePath, inputExtension);
// 		const outputFilePath = path.join(path.dirname(filePath), `${baseName}${outputExtension}`);

// 		const config = {
// 			entry: {
// 				[baseName]: filePath,
// 			},
// 			output: {
// 				path: path.dirname(filePath),
// 				filename: '[name].js',
// 			},
// 			mode: 'production',
// 			optimization: {
// 				splitChunks: false,
// 			},
// 			module: {
// 				rules: [
// 					{
// 						test: testRegex,
// 						use: loaders,
// 					},
// 				],
// 			},
// 			plugins: [
// 				new RemoveEmptyScriptsPlugin(),
// 				new MiniCssExtractPlugin({
// 					filename: `[name]${outputExtension}`,
// 				}),
// 			],
// 		};

// 		webpack(config, (err, stats) => {
// 			if (err || stats.hasErrors()) {
// 				const info = stats ? stats.toJson() : {};
// 				console.error('Webpack Error:', err || info.errors);
// 				console.error('watch-error', `Error processing ${type}: ${err || info.errors}`);
// 			} else {
// 				console.log('watch-update', `${type} processing completed: ${outputFilePath}`);
// 			}
// 		});
// 	} catch (error) {
// 		console.error('watch-error', `Error processing ${type}: ${error}`);
// 	}
// }

// function processJavaScriptFile(filePath, options = {}) {
// 	try {
// 		const {
// 			inputExtension = '.js',
// 			outputExtension = '.min.js',
// 			testRegex = /\.js$/,
// 			loaders,
// 			type = 'JavaScript',
// 		} = options;
// 		const baseName = path.basename(filePath, inputExtension);
// 		const outputFilePath = path.join(path.dirname(filePath), `${baseName}${outputExtension}`);

// 		const config = {
// 			entry: filePath,
// 			output: {
// 				path: path.dirname(filePath),
// 				filename: path.basename(outputFilePath),
// 			},
// 			cache: false,
// 			mode: 'production',
// 			target: 'web',
// 			optimization: {
// 				splitChunks: false,
// 				minimize: true,
// 				minimizer: [
// 					new TerserPlugin({
// 						extractComments: false, // Disable the creation of .LICENSE.txt files
// 						terserOptions: {
// 							format: {
// 								comments: /@license|@preserve|^!/, // Preserve license comments
// 							},
// 						},
// 					}),
// 				],
// 			},
// 			module: {
// 				rules: [
// 					{
// 						test: testRegex,
// 						exclude: /node_modules/,
// 						use: loaders || [
// 							{
// 								loader: 'babel-loader',
// 								options: {
// 									presets: ['@babel/preset-env'],
// 								},
// 							},
// 						],
// 					},
// 				],
// 			},
// 		};


// 		try {
// 			webpack(config, (err, stats) => {
// 				if (err || stats.hasErrors()) {
// 					const info = stats ? stats.toJson() : {};
// 					console.error('Webpack Error:', err || info.errors);
// 					console.error('watch-error', `Error processing ${type}: ${err || info.errors}`);
// 				} else {
// 					console.log('watch-update', `${type} processing completed: ${outputFilePath}`);
// 				}
// 			});
// 		} catch (error) {
// 			console.error('watch-error', `Error processing ${type}: ${error}`);
// 		}
// 	} catch (error) {
// 		console.error('watch-error', `Error processing ${type}: ${error}`);
// 	}
// }

// handleFileChange(filePath);







// const fs = require('fs');
// const { minify } = require('terser');

// const filePath = process.argv[2];

// fs.readFile(filePath, 'utf8', (err, data) => {
//     if (err) {
//         console.error('Error reading file:', err);
//         return;
//     }
//     minify(data).then(result => {
//         fs.writeFile(filePath + '.min.js', result.code, (err) => {
//             if (err) {
//                 console.error('Error writing file:', err);
//             } else {
//                 console.log('Minification successful');
//             }
//         });
//     }).catch(error => {
//         console.error('Minification error:', error);
//     });
// });