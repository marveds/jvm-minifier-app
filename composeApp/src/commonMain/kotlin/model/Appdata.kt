package model

import kotlinx.serialization.*

@Serializable
data class Appdata(
    val folders: List<String> = emptyList(),
    val isWatching: Boolean = false,
    val allowNotify: Boolean = false,
    val nodePath: String = ""
)

//fun getMinifierDataPath(): String {
//    return when {
//        // For macOS and Linux, you can use a standard path (or modify based on your app's preferences)
//        System.getProperty("os.name").contains("Mac") -> {
//            val userDataPath = System.getProperty("user.home") + "/Library/Application Support/YourAppName"
//            Paths.get(userDataPath, "minifierData.json").toString()
//        }
//        System.getProperty("os.name").contains("Windows") -> {
//            // Windows has a different approach for app data path
//            val userDataPath = System.getenv("APPDATA") ?: System.getProperty("user.home")
//            Paths.get(userDataPath, "YourAppName", "minifierData.json").toString()
//        }
//        else -> {
//            // Default for Linux and other Unix-based systems
//            val userDataPath = System.getProperty("user.home") + "/.yourapp"
//            Paths.get(userDataPath, "minifierData.json").toString()
//        }
//    }
//}
//
//val minifierDataPath = getMinifierDataPath()
