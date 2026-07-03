package com.example.sshtool.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sshtool.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TerminalScreen(sshHost: SshHost, initialCommand: String?, onBack: () -> Unit) {
    var output by remember { mutableStateOf(listOf("Connecting to ${sshHost.host}...")) }
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var connectionInfo by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(sshHost) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("TerminalScreen", "Starting connection for ${sshHost.host}")
                val info = SshBridge.connect(sshHost) as? Map<String, Any>
                connectionInfo = info

                if (info != null) {
                    val sh = info["shell"] as net.schmizz.sshj.connection.channel.direct.Session.Shell
                    Log.d("TerminalScreen", "Connected successfully")
                    withContext(Dispatchers.Main) { output = output + "Connected." }
                    
                    val reader = sh.inputStream.bufferedReader()
                    val buffer = CharArray(1024)
                    var read: Int
                    while (reader.read(buffer).also { read = it } != -1) {
                        val rawText = String(buffer, 0, read)
                        val cleanText = rawText.replace("\u001B\\[[\\d;?<>]*[a-zA-Z]".toRegex(), "").replace("\r", "")
                        withContext(Dispatchers.Main) {
                            output = (output + cleanText.split("\n")).takeLast(200)
                        }
                    }
                } else {
                    Log.e("TerminalScreen", "SshBridge returned null info")
                    withContext(Dispatchers.Main) { output = output + "Connection Failed." }
                }
            } catch (e: Exception) {
                Log.e("TerminalScreen", "Error during terminal interaction: ${e.message}", e)
                withContext(Dispatchers.Main) { output = output + "Error: ${e.message}" }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scope.launch(Dispatchers.IO) {
                try {
                    val client = connectionInfo?.get("client") as? net.schmizz.sshj.SSHClient
                    client?.disconnect()
                } catch (e: Exception) {}
            }
        }
    }

    val sendCommand = { cmd: String ->
        scope.launch(Dispatchers.IO) {
            val sh = connectionInfo?.get("shell") as? net.schmizz.sshj.connection.channel.direct.Session.Shell
            sh?.outputStream?.write((cmd + "\n").toByteArray())
            sh?.outputStream?.flush()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Output Terminal
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp)) {
            items(output) { line ->
                Text(text = line, color = TerminalGreen, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }

        // Script Shortcuts
        if (sshHost.scripts.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sshHost.scripts) { script ->
                    Button(
                        onClick = { sendCommand(script.second) },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TechBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(text = script.first, color = TechBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Input Row
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp).background(SurfaceDark, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$ ", color = TerminalPrompt, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            BasicTextField(
                value = input, onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 15.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    sendCommand(input)
                    input = ""
                }),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
            )
        }
    }
}
