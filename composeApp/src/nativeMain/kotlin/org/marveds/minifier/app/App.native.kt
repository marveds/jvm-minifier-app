package org.marveds.minifier.app

import model.Appdata

actual fun startWatchingFolders(
    selectedPaths: List<String>,
    onChange: (String) -> Unit
) {
}

actual fun loadSelectedFolders(): List<String> {
    TODO("Not yet implemented")
}

actual fun saveSelectedFolders(folders: List<String>) {
}

actual fun stopWatchingFolders(onChange: (String) -> Unit) {
}

actual fun clearSelectedFolders() {
}

actual fun loadAppData(): Appdata {
    TODO("Not yet implemented")
}

actual fun saveAppData(appdata: Appdata) {
}