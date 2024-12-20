import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import org.marveds.minifier.app.App
import androidx.compose.ui.window.Tray
import androidx.compose.runtime.*
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.rememberTrayState
import minifierapp.composeapp.generated.resources.Res
import minifierapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.marveds.minifier.app.stopWatchingFolders
import java.awt.SystemTray
import kotlinx.coroutines.*
import org.marveds.minifier.app.AppState
import java.awt.Frame
import java.awt.Window

fun main() = application {
    var isAppVisible by remember { mutableStateOf(true) }
    var isWatching by remember { mutableStateOf(true) }
    var showAlert by remember { mutableStateOf(true) }
    val scope = CoroutineScope(Dispatchers.Default)
    val isMacOS = System.getProperty("os.name").contains("mac", ignoreCase = true)
    var awtWindow: Window? = null

//    val trayState = rememberTrayState()

    scope.launch {
        // Collect changes from AppState.watchStatus
        AppState.watchStatus.collect { isWatchingObs ->
            withContext(Dispatchers.Main) {
                isWatching = isWatchingObs
            }
        }
    }

    if (!SystemTray.isSupported()) {
        println("System tray is not supported")
        return@application
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        stopWatchingFolders(){}
    })

    Tray(
//        state = trayState,
        icon = painterResource(Res.drawable.app_icon),
        menu = {
            Item("Show Dashboard", onClick = {
                if (isMacOS) {
                    (awtWindow as? Frame)?.extendedState = Frame.NORMAL
                } else {
                    isAppVisible = true
                }
            })
            Item("Hide", onClick = {
                if (isMacOS) {
                    (awtWindow as? Frame)?.extendedState = Frame.ICONIFIED
                } else {
                    isAppVisible = false
                }
            })
            Separator()
            Item("View Logs", onClick = {
                if (isMacOS) {
                    (awtWindow as? Frame)?.extendedState = Frame.NORMAL
                }
                AppState.showLogs(true)
            })
            Item("Clear Paths", onClick = { AppState.clearLogs(true) })
            Separator()
            Menu("Watch") {
                CheckboxItem(
                    "Start",
                    checked = isWatching,
                    enabled = !isWatching,
                    onCheckedChange = {
                        isWatching = true
                        // Start watching logic
                        val newStatus = !AppState.watchStatus.value
                        AppState.setWatchStatus(newStatus)
                    }
                )
                CheckboxItem(
                    "Stop",
                    checked = !isWatching,
                    enabled = isWatching,
                    onCheckedChange = {
                        isWatching = false
                        // Stop watching logic
                        val newStatus = !AppState.watchStatus.value
                        AppState.setWatchStatus(newStatus)
                    }
                )
            }
            Menu("Notification") {
                CheckboxItem(
                    "On",
                    checked = showAlert,
                    enabled = !showAlert,
                    onCheckedChange = {
                        showAlert = true
                        AppState.setNotification(true)
                    }
                )
                CheckboxItem(
                    "Off",
                    checked = !showAlert,
                    enabled = showAlert,
                    onCheckedChange = {
                        showAlert = false
                        AppState.setNotification(false)
                    }
                )
            }
            Separator()
            Item("Exit", onClick = { exitApplication() })
//            Item("Notify", onClick = {
//                trayState.sendNotification(
//                    Notification(
//                        title = "Test Notification",
//                        message = "This is a test notification"
//                    )
//                )
//            })
        }
    )

    if (isAppVisible) {
        Window(
            title = "MinifierApp",
            state = rememberWindowState(width = 1000.dp, height = 600.dp),
            onCloseRequest = {
                if (isMacOS) {
                    (awtWindow as? Frame)?.extendedState = Frame.ICONIFIED
                } else {
                    exitApplication()
                }
            },
            icon = painterResource(Res.drawable.app_icon),
        ) {
            LaunchedEffect(Unit) {
                awtWindow = window
                window.minimumSize = Dimension(800, 600)
            }
            App()
        }
    }
}

@Preview
@Composable
fun AppPreview() { App() }