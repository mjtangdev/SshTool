package com.example.sshtool

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.schmizz.sshj.DefaultConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class SshApplication : Application() {
    companion object {
        private var _sshConfig: DefaultConfig? = null
        private val configMutex = Mutex()

        /**
         * Ensures BouncyCastle is registered at the top and returns the SSH config.
         */
        suspend fun getSshConfig(): DefaultConfig {
            return _sshConfig ?: configMutex.withLock {
                _sshConfig ?: withContext(Dispatchers.IO) {
                    // Critical: On Android, we should remove the existing "BC" provider if any
                    // and re-add our modern BouncyCastleProvider at the top to avoid 
                    // "no such algorithm: X25519" errors.
                    Security.removeProvider("BC")
                    Security.insertProviderAt(BouncyCastleProvider(), 1)

                    DefaultConfig().also { _sshConfig = it }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        System.setProperty("java.security.egd", "file:/dev/./urandom")
    }
}
