import androidx.compose.ui.window.ComposeUIViewController
import org.marveds.minifier.app.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
