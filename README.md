# SshTool - High Performance Android SSH Client

SshTool is a lightweight, high-performance SSH client for Android, featuring a hybrid architecture of Jetpack Compose and Native Android Views to ensure instantaneous navigation and smooth terminal interactions.

## 🚀 Key Features

- **Blazing Fast Navigation**: Uses Native XML + View architecture for static screens to eliminate Jetpack Compose's debug-mode performance lag.
- **Modern Dark UI**: A consistent, deep-dark theme with tech-blue accents and refined terminal-style watermarks.
- **Server Management**: Create, edit, and store multiple SSH host configurations.
- **Smart Form Layout**: Intuitive `user@host` input fields and password visibility toggle.
- **Automation Scripts**: Pre-configure one-click commands that appear as shortcuts in the terminal.
- **Integrated Test Tool**: "TEST LINK" functionality in the creation screen to verify connectivity before saving.
- **Modern Security**: Bundled with the latest BouncyCastle provider to support modern encryption algorithms like `X25519`.

## 🛠 Tech Stack & Libraries

### Core Architecture
- **Language**: Kotlin
- **UI Framework**: 
  - **Jetpack Compose**: Powers the dynamic Terminal output.
  - **Native XML/View**: Used for the Host List and Edit screens for "instant-on" performance.
- **Navigation**: State-driven navigation within `MainActivity`.

### Libraries
- **SSH Protocol**: [sshj](https://github.com/hierynomus/sshj) (v0.38.0)
- **Encryption**: [BouncyCastle](https://www.bouncycastle.org/) (v1.78.1)
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) (v2.6.1)
- **Material Components**: [Material Design 3](https://m3.material.io/components)
- **Logging**: [SLF4J Android](https://www.slf4j.org/) & [Timber](https://github.com/JakeWharton/timber)

## 📖 Operation Guide

### 1. Adding a Server
- Click the **ADD SERVER** button on the home screen or the **+** icon in the toolbar.
- Fill in the **Display Name**, **User**, **Host/IP**, and **SSH Password**.
- Use the **Eye Icon** in the password field to verify your input.

### 2. Automation Scripts
- Scroll down to the **Automation Scripts** section.
- Click the **+** icon to add a new command shortcut.
- Enter a name (e.g., "Check Logs") and the actual command (e.g., `tail -f /var/log/syslog`).
- These will appear as clickable buttons inside the terminal for one-tap execution.

### 3. Connection Testing
- Before saving, click **TEST LINK** in the top right menu.
- A dialog will show real-time status: *Initializing Engine -> Connecting -> Success/Failure*.

### 4. Terminal Usage
- Click **CONNECT NOW** on any host card.
- Once connected, type commands in the input field at the bottom.
- Use the pre-configured **Script Buttons** above the keyboard for quick automation.
- Connection is automatically closed when you leave the screen to save battery and resources.

## ⚠️ Troubleshooting

- **Network Timeout**: If you receive a `SocketTimeoutException`, ensure your server's security group/firewall allows inbound traffic on **TCP Port 22** and try switching from WiFi to mobile data.
- **Performance**: For the best experience, run the application in **Release Mode**. Debug mode may introduce slight overhead due to Jetpack Compose's monitoring tools.

---
Developed as a robust, professional-grade tool for server administrators on the go.
