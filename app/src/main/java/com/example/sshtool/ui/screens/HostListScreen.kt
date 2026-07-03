package com.example.sshtool.ui.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

@Composable
fun HostListScreen(
    vm: SshViewModel,
    onAddHost: () -> Unit,
    onEditHost: (SshHostEntity) -> Unit,
    onConnect: (SshHost, String?) -> Unit,
    onFileManager: (SshHost) -> Unit
) {
    val hostsEntity by vm.hosts.collectAsState()
    
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.screen_host_list, null)
            val rv = view.findViewById<RecyclerView>(R.id.rv_hosts)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            val btnAddEmpty = view.findViewById<View>(R.id.btn_add_empty)

            rv.layoutManager = LinearLayoutManager(context)
            val adapter = HostAdapter(
                onEdit = { entity -> onEditHost(entity) },
                onConnect = { host -> onConnect(host, null) },
                onFiles = { host -> onFileManager(host) }
            )
            rv.adapter = adapter
            
            toolbar.inflateMenu(R.menu.menu_host_list)
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_add) {
                    onAddHost()
                    true
                } else false
            }

            btnAddEmpty.setOnClickListener { onAddHost() }

            view
        },
        update = { view ->
            val rv = view.findViewById<RecyclerView>(R.id.rv_hosts)
            val emptyView = view.findViewById<View>(R.id.empty_view)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            
            val adapter = rv.adapter as? HostAdapter
            adapter?.submitList(hostsEntity)
            
            val menu = toolbar.menu
            val addAction = menu.findItem(R.id.action_add)
            
            if (hostsEntity.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                rv.visibility = View.GONE
                addAction?.isVisible = false
            } else {
                emptyView.visibility = View.GONE
                rv.visibility = View.VISIBLE
                addAction?.isVisible = true
            }
        }
    )
}

class HostAdapter(
    private val onEdit: (SshHostEntity) -> Unit,
    private val onConnect: (SshHost) -> Unit,
    private val onFiles: (SshHost) -> Unit
) : RecyclerView.Adapter<HostAdapter.ViewHolder>() {
    private var items = listOf<SshHostEntity>()

    fun submitList(newList: List<SshHostEntity>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_host, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        val scripts = if (item.scriptsJson.isNotBlank()) {
            item.scriptsJson.split("|||").mapNotNull {
                val parts = it.split(">>>")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
        } else emptyList()

        val sshHost = SshHost(item.name, item.host, item.user, item.pass, scripts)
        
        holder.name.text = item.name
        holder.host.text = "${item.user}@${item.host}"
        
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnConnect.setOnClickListener { onConnect(sshHost) }
        holder.btnFiles.setOnClickListener { onFiles(sshHost) }
        
        holder.itemView.setOnClickListener { onConnect(sshHost) }
    }

    override fun getItemCount() = items.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tv_name)
        val host: TextView = v.findViewById(R.id.tv_host)
        val btnEdit: View = v.findViewById(R.id.btn_edit)
        val btnConnect: View = v.findViewById(R.id.btn_connect)
        val btnFiles: View = v.findViewById(R.id.btn_files)
    }
}
