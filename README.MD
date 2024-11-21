# Compose Multiplatform Application

# MinifierApp

MinifierApp is a desktop tool designed to watch specified folders for JavaScript, and LESS file changes. When changes are detected, the app automatically compresses or minifies these files, ensuring your code is always optimized. Built using Kotlin Compose Multiplatform, this app provides a seamless experience across different operating systems.

## Features

- **Real-Time Watching**: Watches folders for changes in JavaScript, and LESS files.
- **Automatic Minification**: Automatically compresses and minifies changed files using Node.js.
- **Cross-Platform Support**: Built with Compose Multiplatform to run on macOS, Linux, and Windows.
- **Configurable Settings**: Allows users to set the Node.js executable path via a settings UI.

## Prerequisites

- **Node.js**: The application requires Node.js for executing the minification script. Make sure Node.js is installed and available in your system path.
- **JDK 14+**: The application requires JDK 14 or newer.

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/marveds/jvm-minifier-app.git
   cd MinifierApp
   ```

2. Build the project using Gradle:

   ```bash
   ./gradlew build
   ```

3. Run the application:

   ```bash
   ./gradlew run
   ```

## How to Use

1. **Configure Node Path**: On the settings screen, enter the path to the Node.js executable if it isn't in the system path.
2. **Watch Folders**: Use the interface to select folders that you want to watch for file changes.
3. **Minification**: The app will automatically minify JavaScript, and LESS files upon detecting any changes.

## Features in Detail

- **Automatic Folder Watching**: Add directories you wish to monitor, and the app will continuously watch for file changes.
- **Minify JavaScript, and LESS**: The app uses a Node.js script to minify modified files.
- **Settings**: The app allows configuration of the Node.js path, ensuring compatibility even if Node.js is not in your system's default PATH.

## Development Setup

1. **Clone the Repo**:
   ```bash
   git clone https://github.com/marveds/jvm-minifier-app.git
   ```
2. **Open in Android Studio**: The project is designed to work with Android Studio for cross-platform development.
3. **Running Locally**: Ensure Node.js is installed. You can check by running:
   ```bash
   node -v
   ```

## Troubleshooting

- **Node.js Not Detected**: If Node.js is not detected, ensure it is installed and available in your system path. You may need to manually set the Node.js path in the app settings.
- **Permissions Issues**: The app may need permission to access the directories for watching and writing minified files.

### Desktop
Run the desktop application: `./gradlew :composeApp:run`
Run desktop UI tests: `./gradlew :composeApp:jvmTest`
Run package application: `./gradlew :composeApp:packageReleaseUberJarForCurrentOs`

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Commit your changes (`git commit -m 'Add a new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Create a Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgements

- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) for making cross-platform development seamless.
- [Node.js](https://nodejs.org/)- (babel, typescript, less, sass, stylus) - for providing a powerful runtime for JavaScript minification.
- 

## Contact

For any questions or suggestions, feel free to open an issue or contact me at marveds2@gmail.com.

