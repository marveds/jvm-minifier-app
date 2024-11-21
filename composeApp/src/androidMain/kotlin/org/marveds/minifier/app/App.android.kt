package org.marveds.minifier.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import android.os.FileObserver
import model.Appdata

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

actual fun startWatchingFolders(selectedPaths: List<String>, onChange: (String) -> Unit) {
    selectedPaths.forEach { path ->
        val fileObserver = object : FileObserver(path, MODIFY) {
            override fun onEvent(event: Int, path: String?) {
                if (event == MODIFY && (path?.endsWith(".js") == true || path?.endsWith(".css") == true)) {
                    onChange(path)
                }
            }
        }
        fileObserver.startWatching()
    }
}

@Preview
@Composable
fun AppPreview() { App() }

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