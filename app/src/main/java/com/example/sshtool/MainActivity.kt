package com.example.sshtool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

data class SshHost(
    val name: String,
    val host: String,
    val user: String,
    val pass: String
)

sealed class Screen {
    object HostList : Screen()
    data class Terminal(val host: SshHost, val initialCommand: String? = null) : Screen()
}

val TechBlue = Color(0xFF00B2FF)
val DarkBackground = Color(0xFF0A0E14)
val SurfaceDark = Color(0xFF161B22)
val TextCyan = Color(0xFF00F2FF)
val TerminalGreen = Color(0xFF4AF626) // Matrix-style soft green
val TerminalPrompt = Color(0xFF00F2FF) // Cyan for prompt

private val DarkColorScheme = darkColorScheme(
    primary = TechBlue,
    secondary = TextCyan,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register BouncyCastle provider for SSH support (x25519, etc.)
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())

        setContent {
            MaterialTheme(colorScheme = DarkColorScheme) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.HostList) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is Screen.HostList -> HostListScreen(
                            onConnect = { host, cmd ->
                                currentScreen = Screen.Terminal(host, cmd)
                            }
                        )
                        is Screen.Terminal -> {
                            val onBack = { currentScreen = Screen.HostList }
                            BackHandler(onBack = onBack)
                            TerminalScreen(
                                sshHost = screen.host,
                                initialCommand = screen.initialCommand,
                                onBack = onBack
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HostListScreen(onConnect: (SshHost, String?) -> Unit) {
    val hosts = listOf(
        // TODO: Move these to a secure local storage or encrypted database
        SshHost("Shs Test Server", "YOUR_SERVER_IP", "YOUR_USERNAME", "YOUR_PASSWORD")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "SSH",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = TerminalGreen
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "TERMINAL",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TechBlue
                )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn {
            items(hosts) { host ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(1.dp, TechBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Top section: Icon + Server Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_server),
                                contentDescription = "Server Icon",
                                modifier = Modifier.size(32.dp),
                                tint = TechBlue
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = host.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextCyan
                                )
                                Text(
                                    text = "${host.user}@${host.host}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Bottom section: Buttons (Independent horizontal layer)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onConnect(host, null) },
                                modifier = Modifier.weight(1f),
                                border = borderStroke(1.dp, TechBlue),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TechBlue)
                            ) {
                                Text("LINK", fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = { onConnect(host, "./deploy.sh") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                            ) {
                                Text("REDEPLOY", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper for border stroke if needed, though border() modifier is fine.
@Composable
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun TerminalScreen(sshHost: SshHost, initialCommand: String?, onBack: () -> Unit) {
    var output by remember { mutableStateOf(listOf("Connecting to ${sshHost.host}...")) }
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var shell by remember { mutableStateOf<net.schmizz.sshj.connection.channel.direct.Session.Shell?>(null) }
    var showRestartDialog by remember { mutableStateOf(false) }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Redeploy Successful") },
            text = { Text("The service has been redeployed and the database has been reset. Would you like to return to the host list?") },
            confirmButton = {
                Button(onClick = {
                    showRestartDialog = false
                    onBack()
                }) {
                    Text("Return to Home")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Stay in Terminal")
                }
            }
        )
    }

    LaunchedEffect(sshHost) {
        scope.launch(Dispatchers.IO) {
            val client = SSHClient()
            try {
                client.addHostKeyVerifier(PromiscuousVerifier())
                client.connect(sshHost.host)
                client.authPassword(sshHost.user, sshHost.pass)
                
                // If there's an initial command (Restart flow), execute it and wait for completion
                initialCommand?.let { cmd ->
                    val session = client.startSession()
                    try {
                        withContext(Dispatchers.Main) {
                            output = output + "Executing: $cmd"
                        }
                        val exec = session.exec(cmd)
                        
                        // Pipe exec output to UI
                        scope.launch(Dispatchers.IO) {
                            val reader = exec.inputStream.bufferedReader(Charsets.UTF_8)
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                withContext(Dispatchers.Main) {
                                    output = output + (line ?: "")
                                }
                            }
                        }
                        
                        exec.join() // Wait for the command to finish
                        if (exec.exitStatus == 0) {
                            withContext(Dispatchers.Main) {
                                output = output + "Command finished successfully."
                                showRestartDialog = true
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                output = output + "Command failed with exit status: ${exec.exitStatus}"
                            }
                        }
                    } finally {
                        session.close()
                    }
                }

                val session = client.startSession()
                session.allocateDefaultPTY()
                val sh = session.startShell()
                shell = sh

                withContext(Dispatchers.Main) {
                    output = output + "Connected to shell."
                }

                // Read output stream
                val inputStream = sh.inputStream
                val reader = inputStream.bufferedReader(Charsets.UTF_8)
                val charBuffer = CharArray(2048)
                var charsRead: Int
                while (reader.read(charBuffer).also { charsRead = it } != -1) {
                    val rawText = String(charBuffer, 0, charsRead)
                    
                    // More comprehensive ANSI filtering:
                    val cleanText = rawText
                        // CSI sequences: ESC [ parameter bytes... final byte
                        // Handles: ?25h, ?25l, ?2004h, 0m, 1;32m etc.
                        .replace("\u001B\\[[\\d;?<>]*[a-zA-Z]".toRegex(), "")
                        // OSC sequences: ESC ] ... ST (or BEL)
                        .replace("\u001B].*?(\u0007|\u001B\\\\)".toRegex(), "")
                        // Handle backspaces (\b) - simple removal
                        .replace("\u0008", "")
                        .replace("\r", "") // Remove carriage returns

                    if (cleanText.isNotEmpty() || rawText.contains("\n")) {
                        withContext(Dispatchers.Main) {
                            val lines = cleanText.split("\n")
                            val newOutput = output.toMutableList()
                            
                            lines.forEachIndexed { index, line ->
                                if (index == 0 && !rawText.startsWith("\n") && newOutput.isNotEmpty()) {
                                    val lastIdx = newOutput.size - 1
                                    newOutput[lastIdx] = newOutput[lastIdx] + line
                                } else {
                                    newOutput.add(line)
                                }
                            }
                            
                            // Limit buffer size to 500 lines
                            output = if (newOutput.size > 500) newOutput.takeLast(500) else newOutput
                        }
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    output = output + "Error: ${e.message}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(output) { line ->
                if (line.isNotBlank()) {
                    Text(
                        text = line,
                        color = if (line.startsWith("$ ") || line.contains("@")) TerminalPrompt else TerminalGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .background(SurfaceDark, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = TerminalPrompt,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        val command = input
                        input = ""
                        scope.launch(Dispatchers.IO) {
                            try {
                                shell?.outputStream?.let {
                                    it.write((command + "\n").toByteArray())
                                    it.flush()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    output = output + "Failed to send: ${e.message}"
                                }
                            }
                        }
                    }
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
            )
        }
    }
}
