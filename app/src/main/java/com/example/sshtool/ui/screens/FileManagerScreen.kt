package com.example.sshtool.ui.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sshtool.*
import com.example.sshtool.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FileManagerScreen(sshHost: SshHost, onBack: () -> Unit) {
    var currentPath by remember { mutableStateOf(".") }
    var displayPath by remember { mutableStateOf("Loading...") }
    var files by remember { mutableStateOf<List<RemoteFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadFiles(path: String) {
        isLoading = true
        scope.launch {
            val (resolvedPath, result) = SshBridge.listFiles(sshHost, path)
            files = result
            displayPath = resolvedPath
            currentPath = resolvedPath // Update current path to the absolute one
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadFiles(".") // Start in current directory (./)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.screen_file_manager, null)
            val rv = view.findViewById<RecyclerView>(R.id.rv_files)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

            rv.layoutManager = LinearLayoutManager(context)
            val adapter = FileAdapter { file ->
                if (file.isDirectory) {
                    loadFiles(file.path)
                }
            }
            rv.adapter = adapter

            toolbar.setNavigationOnClickListener { 
                if (displayPath == "/") {
                    onBack()
                } else {
                    val parentPath = displayPath.substringBeforeLast("/", "").ifEmpty { "/" }
                    loadFiles(parentPath)
                }
            }
            
            view
        },
        update = { view ->
            val rv = view.findViewById<RecyclerView>(R.id.rv_files)
            val tvPath = view.findViewById<TextView>(R.id.tv_current_path)
            val pb = view.findViewById<ProgressBar>(R.id.progress_bar)
            val tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

            toolbar.title = sshHost.name
            tvPath.text = displayPath
            pb.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            val adapter = rv.adapter as? FileAdapter
            adapter?.submitList(files)
            
            tvEmpty.visibility = if (!isLoading && files.isEmpty()) View.VISIBLE else View.GONE
        }
    )
}

class FileAdapter(private val onItemClick: (RemoteFile) -> Unit) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    private var items = listOf<RemoteFile>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun submitList(newList: List<RemoteFile>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        
        if (item.isDirectory) {
            holder.icon.setImageResource(R.drawable.ic_folder)
            holder.info.text = "Directory"
        } else {
            holder.icon.setImageResource(R.drawable.ic_file)
            val sizeStr = formatFileSize(item.size)
            val dateStr = dateFormat.format(Date(item.lastModified))
            holder.info.text = "$sizeStr • $dateStr"
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tv_file_name)
        val info: TextView = v.findViewById(R.id.tv_file_info)
        val icon: ImageView = v.findViewById(R.id.iv_file_icon)
    }
}
