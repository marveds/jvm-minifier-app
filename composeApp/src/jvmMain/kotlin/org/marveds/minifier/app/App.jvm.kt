package org.marveds.minifier.app

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Appdata
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.concurrent.Executors
import minifierapp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.nio.file.Files

val userDataPath: String = System.getProperty("user.home")
val minifierDataPath: Path = Paths.get(userDataPath, "MinifierData", "minifierData.json")
private var nodeProcess: Process? = null

@OptIn(ExperimentalResourceApi::class, DelicateCoroutinesApi::class)
actual fun startWatchingFolders(selectedPaths: List<String>, onChange: (String) -> Unit) {
    try {

        GlobalScope.launch {
            val currentData: Appdata = loadAppData()
            val resourceBytes = Res.readBytes("files/minifier.js")

            val userDataDir = Paths.get(userDataPath, "MinifierData", "scripts").toFile()

            if (!userDataDir.exists()) {
                userDataDir.mkdirs()
            }

            val outputFile = File(userDataDir, "minifier.js")

            outputFile.writeBytes(resourceBytes)

            println("Resource successfully written to ${outputFile.absolutePath}")

            val nodeCommand = listOf(currentData.nodePath, outputFile.absolutePath)
            val processBuilder = ProcessBuilder(nodeCommand)
            processBuilder.redirectErrorStream(true)

            nodeProcess = processBuilder.start()

            // Use a separate thread to handle the process output
            val executor = Executors.newSingleThreadExecutor()
            executor.submit {
                try {
                    nodeProcess?.inputStream?.bufferedReader()?.use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            onChange(line!!)
                        }
                    }
                } catch (e: IOException) {
                    onChange("Error reading Node.js process output: ${e.message}")
                }
            }

            // Another thread to wait for the process and handle exit status
            Executors.newSingleThreadExecutor().submit {
                val exitCode = nodeProcess?.waitFor() ?: -1
                if (exitCode == 143) {
                    onChange("Node.js process was terminated successfully (SIGTERM received).")
                } else if (exitCode != 0) {
                    onChange("Node.js process exited with error. Exit code: $exitCode")
                } else {
                    onChange("Node.js process stopped successfully.")
                }
            }
        }

//        val scriptPath = File(resourceUri).absolutePath
//        val nodeCommand = listOf("node", scriptPath)
//        val processBuilder = ProcessBuilder(nodeCommand)
//        processBuilder.redirectErrorStream(true)
//        // Start the process
//        nodeProcess = processBuilder.start()
//
//        // Use a separate thread to handle the process output
//        val executor = Executors.newSingleThreadExecutor()
//        executor.submit {
//            try {
//                nodeProcess?.inputStream?.bufferedReader()?.use { reader ->
//                    var line: String?
//                    while (reader.readLine().also { line = it } != null) {
//                        onChange(line!!)
//                    }
//                }
//            } catch (e: IOException) {
//                onChange("Error reading Node.js process output: ${e.message}")
//            }
//        }
//
//        // Another thread to wait for the process and handle exit status
//        Executors.newSingleThreadExecutor().submit {
//            val exitCode = nodeProcess?.waitFor() ?: -1
//            if (exitCode == 143) {
//                onChange("Node.js process was terminated successfully (SIGTERM received).")
//            } else if (exitCode != 0) {
//                onChange("Node.js process exited with error. Exit code: $exitCode")
//            } else {
//                onChange("Node.js process stopped successfully.")
//            }
//        }
    } catch (e: Exception) {
        onChange("Node.js is not installed or failed to execute: ${e.message}")
    }
}

//fun createScriptFile(appdata: Appdata) {
//    val file = minifierDataPath.toFile()
//    val updatedJsonString = Json.encodeToString(appdata)
//    file.writeText(updatedJsonString)
//}

actual fun stopWatchingFolders(onChange: (String) -> Unit) {
    nodeProcess?.let {
        it.destroy()
        onChange("Node.js watch process has been stopped.")
    }
}

actual fun loadAppData(): Appdata {
    val file = minifierDataPath.toFile()
    if (file.exists()) {
        val jsonString = file.bufferedReader().use { it.readText() }
        val appData = Json.decodeFromString<Appdata>(jsonString)
        return appData
    } else {
        val userDataDir = Paths.get(userDataPath, "MinifierData", "scripts").toFile()
        if (!userDataDir.exists()) {
            userDataDir.mkdirs()
        }
        val defaultAppData = Appdata()
        saveAppData(defaultAppData)
        return defaultAppData
    }
}

actual fun saveAppData(appdata: Appdata) {
    val file = minifierDataPath.toFile()
    val updatedJsonString = Json.encodeToString(appdata)
    file.writeText(updatedJsonString)
}

@OptIn(DelicateCoroutinesApi::class)
actual fun clearSelectedFolders() {
    try {
        if (Files.exists(minifierDataPath)) {
            GlobalScope.launch {
                val currentData: Appdata = loadAppData()
                val newData = currentData.copy(folders = emptyList())
                saveAppData(newData)
                println("Successfully cleared paths from data file: $minifierDataPath")
            }
//            Files.delete(minifierDataPath)
//            println("Successfully deleted the minifier data file: $minifierDataPath")
        } else {
            println("No minifier data file found at: $minifierDataPath")
        }
    } catch (e: Exception) {
        println("An error occurred while trying to delete the minifier data file: ${e.message}")
    }
}

actual fun loadSelectedFolders(): List<String> {
    val file = minifierDataPath.toFile()
    if (file.exists()) {
        val jsonString = file.bufferedReader().use { it.readText() }
        val selectedFolders = Json.decodeFromString<Appdata>(jsonString)
        return selectedFolders.folders
    }
    return emptyList()
}

actual fun saveSelectedFolders(folders: List<String>) {
    val file = minifierDataPath.toFile()

    val currentData: Appdata = if (file.exists() && file.readText().isNotBlank()) {
        try {
            val jsonString = file.readText()
            Json.decodeFromString<Appdata>(jsonString)
        } catch (e: Exception) {
            Appdata()
        }
    } else {
        Appdata()
    }

    val updatedData = currentData.copy(
        folders = folders
    )

    val updatedJsonString = Json.encodeToString(updatedData)
    file.writeText(updatedJsonString)
}