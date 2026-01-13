package com.limxinhuang.smallthings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limxinhuang.smallthings.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var repository: TaskRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = TaskRepository(requireContext())

        setupRecyclerView()
        loadTasks()

        // 设置浮动按钮点击事件
        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), CreateTaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(emptyList()) { task ->
            // 点击卡片进入编辑页面
            val intent = Intent(requireContext(), EditTaskActivity::class.java)
            intent.putExtra("task_id", task.id)
            startActivity(intent)
        }

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
            itemAnimator = null
        }

        // 添加滑动监听
        val itemTouchHelperCallback = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                lifecycleScope.launch {
                    val tasks = repository.getAllTasks()
                    if (position >= 0 && position < tasks.size) {
                        val task = tasks[position]
                        startTaskExecution(task)
                        // 刷新列表恢复状态
                        loadTasks()
                    }
                }
            }
        }

        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvTasks)
    }

    private fun startTaskExecution(task: Task) {
        val intent = Intent(requireContext(), TaskExecutionActivity::class.java)
        intent.putExtra("task_id", task.id)
        intent.putExtra("task_name", task.name)
        intent.putExtra("task_color", task.colorCode)
        intent.putExtra("task_duration", task.durationMinutes)
        startActivity(intent)
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val tasks = repository.getAllTasks()
            taskAdapter.updateTasks(tasks)

            // 显示或隐藏空状态
            if (tasks.isEmpty()) {
                binding.tvSubtitle.visibility = View.VISIBLE
                binding.rvTasks.visibility = View.GONE
            } else {
                binding.tvSubtitle.visibility = View.GONE
                binding.rvTasks.visibility = View.VISIBLE
            }
        }
    }
}
