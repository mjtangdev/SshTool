package com.example.sshtool

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier

/**
 * A Bridge class to isolate SSH library loading from the UI.
 */
object SshBridge {
    private const val TAG = "SshBridge"

    suspend fun testConnection(host: String, user: String, pass: String): String = withContext(Dispatchers.IO) {
        val client = SSHClient(SshApplication.getSshConfig())
        try {
            val cleanedHost = host.trim()
            val cleanedUser = user.trim()
            val cleanedPass = pass.trim()
            
            Log.d(TAG, "TEST: Connecting to '$cleanedHost' as '$cleanedUser'")
            client.addHostKeyVerifier(PromiscuousVerifier())
            client.connectTimeout = 10000
            
            val hostParts = cleanedHost.split(":")
            val ip = hostParts[0]
            val port = if (hostParts.size > 1) hostParts[1].toInt() else 22
            
            client.connect(ip, port)
            
            // Explicitly try password authentication
            client.authPassword(cleanedUser, cleanedPass)
            
            if (client.isAuthenticated) {
                "Connection successful!"
            } else {
                "Authentication failed!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Test failed: ${e.message}", e)
            val msg = e.message ?: e.javaClass.simpleName
            if (e is java.net.SocketTimeoutException) {
                "Failed: Network Timeout. Check if port is reachable."
            } else {
                "Failed: $msg"
            }
        } finally {
            try { client.disconnect() } catch (e: Exception) {}
        }
    }

    /**
     * Lists files and returns the canonical path and the file list.
     */
    suspend fun listFiles(host: SshHost, path: String): Pair<String, List<RemoteFile>> = withContext(Dispatchers.IO) {
        val client = SSHClient(SshApplication.getSshConfig())
        try {
            client.addHostKeyVerifier(PromiscuousVerifier())
            client.connect(host.host.trim())
            client.authPassword(host.user.trim(), host.pass.trim())
            
            client.newSFTPClient().use { sftp ->
                // Resolve the real path (e.g. convert "." to "/home/admin")
                val canonicalPath = sftp.canonicalize(path)
                
                val list = sftp.ls(canonicalPath).map { entry ->
                    RemoteFile(
                        name = entry.name,
                        path = if (canonicalPath.endsWith("/")) canonicalPath + entry.name else "$canonicalPath/${entry.name}",
                        isDirectory = entry.isDirectory,
                        size = entry.attributes.size,
                        lastModified = entry.attributes.mtime * 1000L
                    )
                }.toMutableList()

                // Remove . and .. if they exist to handle them manually
                list.removeAll { it.name == "." || it.name == ".." }
                
                val sortedList = list.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
                
                val finalList = mutableListOf<RemoteFile>()
                // Add .. at the top if not at root
                if (canonicalPath != "/") {
                    val parentPath = canonicalPath.substringBeforeLast("/", "").ifEmpty { "/" }
                    finalList.add(RemoteFile("..", parentPath, true, 0, 0))
                }
                finalList.addAll(sortedList)
                
                canonicalPath to finalList
            }
        } catch (e: Exception) {
            Log.e(TAG, "SFTP List failed: ${e.message}")
            path to emptyList()
        } finally {
            try { client.disconnect() } catch (e: Exception) {}
        }
    }

    suspend fun connect(host: SshHost): Any? = withContext(Dispatchers.IO) {
        val client = SSHClient(SshApplication.getSshConfig())
        try {
            val cleanedHost = host.host.trim()
            val cleanedUser = host.user.trim()
            val cleanedPass = host.pass.trim()
            
            Log.d(TAG, "REAL: Connecting to '$cleanedHost' as '$cleanedUser'")
            client.addHostKeyVerifier(PromiscuousVerifier())
            client.connectTimeout = 15000
            
            val hostParts = cleanedHost.split(":")
            val ip = hostParts[0]
            val port = if (hostParts.size > 1) hostParts[1].toInt() else 22
            
            client.connect(ip, port)
            client.authPassword(cleanedUser, cleanedPass)
            
            val session = client.startSession()
            session.allocateDefaultPTY()
            val sh = session.startShell()
            
            Log.d(TAG, "Shell started successfully")
            mapOf("client" to client, "shell" to sh)
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}", e)
            try { client.disconnect() } catch (ex: Exception) {}
            null
        }
    }
}
