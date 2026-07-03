package com.example.sshtool

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier

object SshManager {
    /**
     * Test connection to a host.
     * This is an expensive call that loads the SSH library classes.
     */
    suspend fun testConnection(ip: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val config = SshApplication.getSshConfig()
            val client = SSHClient(config)
            client.addHostKeyVerifier(PromiscuousVerifier())
            client.connect(ip)
            client.disconnect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a new SSH client using the preheated config.
     */
    suspend fun createClient(): SSHClient {
        val config = SshApplication.getSshConfig()
        return SSHClient(config)
    }
}
