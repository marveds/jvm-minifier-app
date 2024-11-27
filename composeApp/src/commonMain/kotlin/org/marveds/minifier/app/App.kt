package org.marveds.minifier.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.marveds.minifier.app.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.layout.onGloballyPositioned
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.*
import compose.icons.fontawesomeicons.solid.*
import kotlinx.coroutines.*
import kotlinx.coroutines.suspendCancellableCoroutine
import model.Appdata
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Toolkit
import java.awt.AWTException
import javax.swing.*
import java.awt.*
import java.net.URI

object AppState {
    private val _watchStatus = MutableStateFlow(false)
    val watchStatus: StateFlow<Boolean> = _watchStatus

    private val _viewLogs = MutableStateFlow(false)
    val viewLogs: StateFlow<Boolean> = _viewLogs

    private val _clearLogs = MutableStateFlow(false)
    val clearLogs: StateFlow<Boolean> = _clearLogs

    private val _allowNotification = MutableStateFlow(true)
    val allowNotification: StateFlow<Boolean> = _allowNotification

    fun setWatchStatus(value: Boolean) {
        _watchStatus.value = value
    }

    fun showLogs(value: Boolean) {
        _viewLogs.value = value
    }

    fun clearLogs(value: Boolean) {
        _clearLogs.value = value
    }

    fun setNotification(value: Boolean) {
        _allowNotification.value = value
    }
}

@Composable
internal fun App() = AppTheme {
    Column(
        modifier = Modifier
            .fillMaxSize(),
//            .windowInsetsPadding(WindowInsets.safeDrawing)
//            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MinifierApp()
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MinifierApp() {
    var isSidebarVisible by remember { mutableStateOf(true) }
    val transition = updateTransition(targetState = isSidebarVisible, label = "Sidebar Transition")
    val sidebarWidth by transition.animateDp(label = "Sidebar Width") { visible -> if (visible) 200.dp else 0.dp }
    val sidebarAlpha by transition.animateFloat(label = "Sidebar Alpha") { visible -> if (visible) 1f else 0f }
    var showFolderSelection by remember { mutableStateOf(true) }
    var watchStatus by remember { mutableStateOf(false) }
    var clearData by remember { mutableStateOf(false) }
    var isListEmpty by remember { mutableStateOf(false) }
    var isFolderListEmpty by remember { mutableStateOf(false) }
    var folderPaths by remember { mutableStateOf(TextFieldValue()) }
    var selectedFolderPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSettings by remember { mutableStateOf(false) }
    var settingsUpdated by remember { mutableStateOf(false) }
    var allowInput by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current

    val pickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { folders ->
            scope.launch {
                val newlySelectedPaths = folders.mapNotNull { it.getPath(context) }
                val combinedPaths = (selectedFolderPaths + newlySelectedPaths).distinct()
                if (combinedPaths.isNotEmpty()){
                    selectedFolderPaths = combinedPaths
                    saveFolderList(selectedFolderPaths){
                        isListEmpty = true
//                        AppState.setWatchStatus(true)
                        scope.launch {
                            val currentData: Appdata = loadAppData()
                            val updatedData = currentData.copy(isWatching = AppState.watchStatus.value)
                            saveAppData(updatedData)
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit, settingsUpdated) {
        if (!isNodeInstalled()) {
            isFolderListEmpty = false
            allowInput = false
            isListEmpty = false
            logs+= "Node.js is not installed or could not be found.\nIf it already installed please add path to settings.\nIf not installed please download and install from https://nodejs.org/. \nThen add path to settings."
            showFolderSelection = false
            SwingUtilities.invokeLater {
                val frame = JFrame("Node.js Installation")
                frame.layout = FlowLayout()

                val message = """
                    Node.js is not installed or could not be found.<br>
                    If it is already installed, please add the path to settings.<br>
                    If not installed, please download and install from: 
                    <a href="https://nodejs.org/">https://nodejs.org/</a>
                    <br>Then add path to settings.
                """.trimIndent()

                val label = JLabel("<html>$message</html>")
                label.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                label.addMouseListener(object : java.awt.event.MouseAdapter() {
                    override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(URI("https://nodejs.org/"))
                        }
                    }
                })

                frame.add(label)
                frame.setSize(600, 200)
                frame.isAlwaysOnTop = true
                frame.isVisible = true
            }
        } else {
            logs+= "Node.js is available. Proceeding with the application.\n"
            val appData: Appdata = loadAppData()
            selectedFolderPaths = appData.folders
            isListEmpty = appData.folders.isNotEmpty()
            AppState.setWatchStatus(appData.isWatching)
            AppState.setNotification(appData.allowNotify)
            allowInput = true

            scope.launch {
                AppState.watchStatus.collect { isWatchingObs ->
                    withContext(Dispatchers.Main) {
                        watchStatus = isWatchingObs
                    }
                }
            }

            scope.launch {
                AppState.viewLogs.collect { viewLogsObs ->
                    withContext(Dispatchers.Main) {
                        showFolderSelection = !viewLogsObs
                    }
                }
            }

            scope.launch {
                AppState.clearLogs.collect { clearLogsObs ->
                    withContext(Dispatchers.Main) {
                        if(clearLogsObs){
                            clearData = true
                        }
                    }
                }
            }

            scope.launch {
                AppState.allowNotification.collect { allowNotificationObs ->
                    withContext(Dispatchers.Main) {
                        val currentData: Appdata = loadAppData()
                        val updatedData = currentData.copy(allowNotify = allowNotificationObs)
                        saveAppData(updatedData)
                    }
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        if (isSidebarVisible || sidebarWidth > 0.dp) {
            Column(
                modifier = Modifier
                    .width(sidebarWidth)
                    .fillMaxHeight()
                    .background(Color.White)
//                    .background(Color.LightGray.copy(alpha = sidebarAlpha))
                    .padding(16.dp)
            ) {
                TextButton(onClick = {
                        showSettings = false
                        showFolderSelection = true
                        AppState.showLogs(!showFolderSelection)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ){
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Folder Selection",
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                        showSettings = false
                        showFolderSelection = false
                        AppState.showLogs(!showFolderSelection)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "View Logs",
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                        showSettings = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Settings",
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(16.dp)
        ) {
            // Top Menu Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                IconButton(onClick = { isSidebarVisible = !isSidebarVisible }) {
                    Icon(FontAwesomeIcons.Solid.Bars,
                        modifier = Modifier.padding(8.dp),
                        contentDescription = "Menu Icon",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("|")
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    enabled = isListEmpty,
                    onClick = {
                        AppState.setWatchStatus(!AppState.watchStatus.value)
                        scope.launch {
                            val currentData: Appdata = loadAppData()
                            val updatedData = currentData.copy(isWatching = AppState.watchStatus.value)
                            saveAppData(updatedData)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (watchStatus) FontAwesomeIcons.Solid.Eye else FontAwesomeIcons.Solid.EyeSlash,
                        modifier = Modifier.padding(8.dp),
                        contentDescription = "Watch Status Icon",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    clearData = true
                }) {
                    Icon(FontAwesomeIcons.Solid.Trash, modifier = Modifier.padding(8.dp), contentDescription = "Clear Data Button", tint = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {showSettings = !showSettings}) {
                    Icon(
                        Icons.Default.Settings,
                        tint = Color.Gray,
                        modifier = Modifier.padding(8.dp),
                        contentDescription = "Setting Icon"
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                thickness = 2.dp,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            if (showSettings){
                SettingsScreen(
                    saveData = {
                        val currentData: Appdata = loadAppData()
                        val updatedData = currentData.copy(nodePath = it)
                        saveAppData(updatedData)
                    },
                    onSaveSettings = { settingsUpdated = !settingsUpdated }
                )
            }else{
                if (showFolderSelection) {
                    Text(
                        "Select Folders to Watch (1 per line)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    BasicTextField(
                        enabled = allowInput,
                        value = folderPaths,
                        onValueChange = {
                            folderPaths = it
                            isFolderListEmpty = folderPaths.text.isNotEmpty()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.White, MaterialTheme.shapes.small)
                            .padding(8.dp)
                    )
                    Row(modifier = Modifier.padding(top = 16.dp)) {
                        IconButton(
                            enabled = isFolderListEmpty,
                            onClick = {
                                selectedFolderPaths = folderPaths.text.lines()
                                isListEmpty = true
                                watchStatus = true
                                scope.launch {
                                    val currentData: Appdata = loadAppData()
                                    val updatedData = currentData.copy(
                                        folders = selectedFolderPaths,
                                        isWatching = watchStatus
                                    )
                                    saveAppData(updatedData)
                                }
                            }
                        ) {
                            Icon(FontAwesomeIcons.Solid.Keyboard, modifier = Modifier.padding(8.dp), contentDescription = "Add Folder Button", tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            enabled = allowInput,
                            onClick = { pickerLauncher.launch() }
                        ) {
                            Icon(FontAwesomeIcons.Solid.FolderOpen, modifier = Modifier.padding(8.dp), contentDescription = "Select Folder Button", tint = Color.Gray)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(5.dp))
                    ) {
                        ShowSelectedFolders(
                            selectedFolderPaths,
                            onRemoveFolder = { folderPath ->
                                selectedFolderPaths = selectedFolderPaths.filter { it != folderPath }
                                if (selectedFolderPaths.isEmpty()){
                                    clearData = true
                                } else {
                                    scope.launch {
                                        saveFolderList(selectedFolderPaths) {}
                                    }
                                }
                            }
                        )
                    }
                } else {
                    UpdateLog(
                        logs,
                        onClearlogsClicked = {
                            logs = ""
                            sendDesktopNotification("Logs Cleared", "Info logs have been cleared.")
                        },
                    )
                }
            }
        }
    }

    LaunchedEffect(clearData) {
        if(clearData){
            AppState.setWatchStatus(false)
            isListEmpty = false
            isFolderListEmpty = false
            folderPaths = TextFieldValue()
            selectedFolderPaths = emptyList()
            clearSelectedFolders()
            clearData = false
            AppState.clearLogs(false)
            sendDesktopNotification("Data Cleared","All Saved data Cleared")
        }
    }

    LaunchedEffect(watchStatus) {
        if (watchStatus){

//            try {
//                val resourceUri = Res.getUri("files/minifier.js")
//
//                // Get the input stream of the resource using classLoader
//                val inputStream: InputStream = this::class.java.classLoader.getResourceAsStream(resourceUri)
//                    ?: throw FileNotFoundException("Resource not found: $resourceUri")
//
//                // Create a temporary file to store the resource
//                val tempFile = Files.createTempFile("minifier", ".js").toFile()
//
//                // Copy the content from the input stream to the temp file
//                inputStream.use { input ->
//                    tempFile.outputStream().use { output ->
//                        input.copyTo(output)
//                    }
//                }
//                println(tempFile)
//            } catch (e: Exception) {
//                println(e.printStackTrace())
//            }


            try {
                startWatchingFolders(selectedFolderPaths) { changedFile ->
                    logs += "$changedFile\n"
                    println(logs)
                }
            } catch (e: Exception) {
                logs += "Error starting watcher: ${e.message}\n"
                println(logs)
            }
        }else{
            stopWatchingFolders(){ changedFile ->
                logs += "Stopped watching all folders\n"
                logs += "$changedFile\n"
                println(logs)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    saveData: (String) -> Unit,
    onSaveSettings: () -> Unit,
) {
    var nodePath by remember { mutableStateOf("") }

    LaunchedEffect(Unit){
        val appData: Appdata = loadAppData()
        nodePath = appData.nodePath
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Enter node path",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

//            val focusRequester = remember { FocusRequester() }

            BasicTextField(
                value = nodePath,
                onValueChange = {
                    nodePath = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, MaterialTheme.shapes.small)
                    .padding(8.dp)
//                    .focusRequester(focusRequester)
//                    .onFocusChanged {
//                        if (!it.isFocused) {
//                            saveData(nodePath)
//                        }
//                    }
            )
        }

        // Bottom horizontal bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
//                .background(Color.Gray)
                .align(Alignment.BottomCenter)
        ) {
            Button(onClick = {
                saveData(nodePath)
                sendDesktopNotification("Settings Saved","Settings saved successfully")
                onSaveSettings()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Settings")
            }
//            Text(
//                text = "Settings Saved",
//                color = Color.White,
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .alpha(0.8f) // Set transparency if needed
//            )
        }
    }
}

@Composable
fun UpdateLog(
    logs: String,
    onClearlogsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // Log View Section
        Text(
            "Output Logs",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        IconButton(onClick = onClearlogsClicked) {
            Icon(
                FontAwesomeIcons.Regular.TrashAlt,
                modifier = Modifier.padding(8.dp),
                contentDescription = "Clear Logs Button"
            )
        }

        // Scrollable Box to show logs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.White, MaterialTheme.shapes.small)
                .padding(8.dp)
                .verticalScroll(scrollState) // Make the Box scrollable
        ) {
            Text(
                text = logs,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(8.dp)
                    .onGloballyPositioned {
                        scope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
            )
        }
    }
}

@Composable
fun ShowSelectedFolders(
    selectedFolderPaths: List<String>,
    onRemoveFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        selectedFolderPaths.forEach { path ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(bottom = 4.dp)
            ) {
                IconButton(onClick = { onRemoveFolder(path) }) {
                    Icon(
                        FontAwesomeIcons.Regular.TrashAlt,
                        contentDescription = "Remove Folder",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = path,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

suspend fun saveFolderList(
    selectedFolderPaths: List<String>,
    onComplete:() -> Unit
): Unit = suspendCancellableCoroutine { continuation ->
    try {
        val currentData: Appdata = loadAppData()
        val updatedData = currentData.copy(
            folders = selectedFolderPaths
        )
        continuation.resume(saveAppData(updatedData))
        onComplete()
    } catch (e: Exception) {
        continuation.resumeWithException(e)
    }
}

fun isNodeInstalled(): Boolean {
    return try {
        val currentData: Appdata = loadAppData()

        // Use ProcessBuilder to execute "node -v"
        val process = ProcessBuilder(currentData.nodePath, "-v")
            .redirectErrorStream(true)
            .start()

        // Capture output from the process
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = reader.readLine()

        process.waitFor()  // Wait for the process to finish

        // Check if the output contains a version number (e.g., "v16.13.0")
        if (output != null && output.startsWith("v")) {
            println("Node.js is installed: $output")
            true
        } else {
            sendDesktopNotification("Node Installation","Node.js is not installed.\nPlease install and add path to settings.")
            false
        }
    } catch (e: Exception) {
        println("Error checking Node.js installation: ${e.message}")
        false
    }
}

fun sendDesktopNotification(title: String, message: String) {
    if (!AppState.allowNotification.value){
        return
    }

    if (SystemTray.isSupported()) {
        val tray = SystemTray.getSystemTray()
        val image = Toolkit.getDefaultToolkit().createImage("icon.png")
        val trayIcon = TrayIcon(image, "Minifier-App")

        try {
            tray.add(trayIcon)
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
        } catch (e: AWTException) {
            e.printStackTrace()
        }
    } else {
        println("System tray not supported.")
    }
}

expect fun loadAppData(): Appdata

expect fun saveAppData(appdata: Appdata)

expect fun loadSelectedFolders(): List<String>

expect fun saveSelectedFolders(folders: List<String>)

expect fun clearSelectedFolders()

expect fun startWatchingFolders(selectedPaths: List<String>, onChange: (String) -> Unit)

expect fun stopWatchingFolders(onChange: (String) -> Unit)

//@Composable
//fun DefaultApp(modifier: Modifier = Modifier) {
//    Text(
//        text = stringResource(Res.string.cyclone),
//        fontFamily = FontFamily(Font(Res.font.IndieFlower_Regular)),
//        style = MaterialTheme.typography.displayLarge
//    )
//
//    var isRotating by remember { mutableStateOf(false) }
//
//    val rotate = remember { Animatable(0f) }
//    val target = 360f
//    if (isRotating) {
//        LaunchedEffect(Unit) {
//            while (isActive) {
//                val remaining = (target - rotate.value) / target
//                rotate.animateTo(target, animationSpec = tween((1_000 * remaining).toInt(), easing = LinearEasing))
//                rotate.snapTo(0f)
//            }
//        }
//    }
//
//    Image(
//        modifier = Modifier
//            .size(250.dp)
//            .padding(16.dp)
//            .run { rotate(rotate.value) },
//        imageVector = vectorResource(Res.drawable.ic_cyclone),
//        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
//        contentDescription = null
//    )
//
//    ElevatedButton(
//        modifier = Modifier
//            .padding(horizontal = 8.dp, vertical = 4.dp)
//            .widthIn(min = 200.dp),
//        onClick = { isRotating = !isRotating },
//        content = {
//            Icon(vectorResource(Res.drawable.ic_rotate_right), contentDescription = null)
//            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
//            Text(
//                stringResource(if (isRotating) Res.string.stop else Res.string.run)
//            )
//        }
//    )
//
//    var isDark by LocalThemeIsDark.current
//    val icon = remember(isDark) {
//        if (isDark) Res.drawable.ic_light_mode
//        else Res.drawable.ic_dark_mode
//    }
//
//    ElevatedButton(
//        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
//        onClick = { isDark = !isDark },
//        content = {
//            Icon(vectorResource(icon), contentDescription = null)
//            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
//            Text(stringResource(Res.string.theme))
//        }
//    )
//
//    val uriHandler = LocalUriHandler.current
//    TextButton(
//        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
//        onClick = { uriHandler.openUri("https://github.com/terrakok") },
//    ) {
//        Text(stringResource(Res.string.open_github))
//    }
//}