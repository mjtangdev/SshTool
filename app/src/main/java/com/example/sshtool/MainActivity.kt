package com.example.sshtool

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sshtool.ui.screens.CreateHostScreen
import com.example.sshtool.ui.screens.HostListScreen
import com.example.sshtool.ui.screens.TerminalScreen
import com.example.sshtool.ui.screens.FileManagerScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data structures
data class SshHost(
    val name: String,
    val host: String,
    val user: String,
    val pass: String,
    val scripts: List<Pair<String, String>> = emptyList()
)

sealed class Screen {
    object HostList : Screen()
    object CreateHost : Screen()
    data class EditHost(val entity: SshHostEntity) : Screen()
    data class Terminal(val host: SshHost, val initialCommand: String? = null) : Screen()
    data class FileManager(val host: SshHost) : Screen()
}

// Colors
val TechBlue = Color(0xFF00B2FF)
val DarkBackground = Color(0xFF0A0E14)
val SurfaceDark = Color(0xFF161B22)
val TextCyan = Color(0xFF00F2FF)
val TerminalGreen = Color(0xFF4AF626)
val TerminalPrompt = Color(0xFF00F2FF)

private val DarkColorScheme = darkColorScheme(
    primary = TechBlue,
    secondary = TextCyan,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

class SshViewModel(application: Application) : AndroidViewModel(application) {
    private val _hosts = MutableStateFlow<List<SshHostEntity>>(emptyList())
    val hosts: StateFlow<List<SshHostEntity>> = _hosts

    fun fetchHosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = SshDatabase.getDatabase(getApplication())
            db.sshHostDao().getAllHosts().collect {
                _hosts.emit(it)
            }
        }
    }

    fun addHost(name: String, host: String, user: String, pass: String, id: Int = 0, scriptsJson: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val db = SshDatabase.getDatabase(getApplication())
            db.sshHostDao().insertHost(SshHostEntity(id = id, name = name, host = host, user = user, pass = pass, scriptsJson = scriptsJson))
        }
    }
}

class MainActivity : AppCompatActivity() {
    private val viewModel: SshViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.HostList) }

            MaterialTheme(colorScheme = DarkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is Screen.HostList -> {
                            LaunchedEffect(Unit) { viewModel.fetchHosts() }
                            HostListScreen(
                                vm = viewModel,
                                onAddHost = { currentScreen = Screen.CreateHost },
                                onEditHost = { entity -> currentScreen = Screen.EditHost(entity) },
                                onConnect = { host, cmd -> currentScreen = Screen.Terminal(host, cmd) },
                                onFileManager = { host -> currentScreen = Screen.FileManager(host) }
                            )
                        }
                        is Screen.CreateHost -> {
                            BackHandler { currentScreen = Screen.HostList }
                            CreateHostScreen(
                                vm = viewModel,
                                onBack = { currentScreen = Screen.HostList },
                                onSave = { currentScreen = Screen.HostList }
                            )
                        }
                        is Screen.EditHost -> {
                            BackHandler { currentScreen = Screen.HostList }
                            CreateHostScreen(
                                vm = viewModel,
                                initialEntity = screen.entity,
                                onBack = { currentScreen = Screen.HostList },
                                onSave = { currentScreen = Screen.HostList }
                            )
                        }
                        is Screen.Terminal -> {
                            BackHandler { currentScreen = Screen.HostList }
                            TerminalScreen(
                                sshHost = screen.host,
                                initialCommand = screen.initialCommand,
                                onBack = { currentScreen = Screen.HostList }
                            )
                        }
                        is Screen.FileManager -> {
                            BackHandler { currentScreen = Screen.HostList }
                            FileManagerScreen(
                                sshHost = screen.host,
                                onBack = { currentScreen = Screen.HostList }
                            )
                        }
                    }
                }
            }
        }
    }
}
