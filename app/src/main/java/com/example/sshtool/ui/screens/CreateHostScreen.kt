package com.example.sshtool.ui.screens

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sshtool.*
import com.example.sshtool.R
import kotlinx.coroutines.launch

@Composable
fun CreateHostScreen(
    vm: SshViewModel, 
    initialEntity: SshHostEntity? = null,
    onBack: () -> Unit, 
    onSave: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.screen_create_host, null)
            
            val etName = view.findViewById<EditText>(R.id.et_name)
            val etUser = view.findViewById<EditText>(R.id.et_user)
            val etHost = view.findViewById<EditText>(R.id.et_host)
            val etPass = view.findViewById<EditText>(R.id.et_pass)
            val btnSave = view.findViewById<Button>(R.id.btn_save)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            val btnAddScript = view.findViewById<ImageButton>(R.id.btn_add_script)
            val llScriptsContainer = view.findViewById<LinearLayout>(R.id.ll_scripts_container)

            val updateScriptLabels = {
                for (i in 0 until llScriptsContainer.childCount) {
                    val scriptView = llScriptsContainer.getChildAt(i)
                    val label = scriptView.findViewById<TextView>(R.id.tv_script_label)
                    label.text = "Script $i"
                }
            }

            val addScriptField = { sName: String, sContent: String ->
                val scriptView = inflater.inflate(R.layout.item_script_input, llScriptsContainer, false)
                val etSName = scriptView.findViewById<EditText>(R.id.et_script_name)
                val etSContent = scriptView.findViewById<EditText>(R.id.et_script_content)
                val btnRemove = scriptView.findViewById<ImageButton>(R.id.btn_remove_script)
                
                etSName.setText(sName)
                etSContent.setText(sContent)
                
                btnRemove.setOnClickListener {
                    llScriptsContainer.removeView(scriptView)
                    updateScriptLabels()
                }
                
                llScriptsContainer.addView(scriptView)
                updateScriptLabels()
            }

            btnAddScript.setOnClickListener { addScriptField("", "") }

            // Handle data loading/initialization
            if (initialEntity != null) {
                toolbar.title = "Edit Server"
                etName.setText(initialEntity.name)
                etUser.setText(initialEntity.user)
                etHost.setText(initialEntity.host)
                etPass.setText(initialEntity.pass)
                
                if (initialEntity.scriptsJson.isNotBlank()) {
                    initialEntity.scriptsJson.split("|||").forEach { scriptStr ->
                        val parts = scriptStr.split(">>>")
                        if (parts.size == 2) addScriptField(parts[0], parts[1])
                    }
                } else {
                    addScriptField("", "")
                }
            } else {
                addScriptField("", "")
            }

            toolbar.setNavigationOnClickListener { onBack() }
            toolbar.inflateMenu(R.menu.menu_create_host)
            val testItem = toolbar.menu.findItem(R.id.action_test)

            val updateStates = {
                val isFormValid = etName.text.isNotBlank() && 
                                etUser.text.isNotBlank() && 
                                etHost.text.isNotBlank() && 
                                etPass.text.isNotBlank()
                
                testItem.isEnabled = isFormValid
                val color = if (isFormValid) android.graphics.Color.parseColor("#00B2FF") 
                            else android.graphics.Color.parseColor("#40FFFFFF") 
                
                val spanString = android.text.SpannableString(testItem.title)
                spanString.setSpan(android.text.style.ForegroundColorSpan(color), 0, spanString.length, 0)
                testItem.title = spanString
                
                btnSave.isEnabled = isFormValid
                btnSave.alpha = if (isFormValid) 1.0f else 0.5f
            }

            val watcher = object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) { updateStates() }
            }

            etName.addTextChangedListener(watcher)
            etUser.addTextChangedListener(watcher)
            etHost.addTextChangedListener(watcher)
            etPass.addTextChangedListener(watcher)
            
            updateStates()

            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_test) {
                    val host = etHost.text.toString()
                    val user = etUser.text.toString()
                    val pass = etPass.text.toString()
                    scope.launch {
                        android.widget.Toast.makeText(context, "Testing connection...", android.widget.Toast.LENGTH_SHORT).show()
                        val result = SshBridge.testConnection(host, user, pass)
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Connection Test")
                            .setMessage(result)
                            .setPositiveButton("Close", null)
                            .show()
                    }
                    true
                } else false
            }

            btnSave.setOnClickListener {
                val name = etName.text.toString()
                val ip = etHost.text.toString()
                val user = etUser.text.toString()
                val pass = etPass.text.toString()
                
                val scripts = mutableListOf<String>()
                for (i in 0 until llScriptsContainer.childCount) {
                    val scriptView = llScriptsContainer.getChildAt(i)
                    val sName = scriptView.findViewById<EditText>(R.id.et_script_name).text.toString()
                    val sContent = scriptView.findViewById<EditText>(R.id.et_script_content).text.toString()
                    if (sName.isNotBlank()) scripts.add("$sName>>>$sContent")
                }
                val scriptsJson = scripts.joinToString("|||")

                if (name.isNotBlank() && ip.isNotBlank()) {
                    vm.addHost(name, ip, user, pass, initialEntity?.id ?: 0, scriptsJson)
                    onSave()
                }
            }
            
            view
        }
    )
}
