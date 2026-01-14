package com.limxinhuang.smallthings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.limxinhuang.smallthings.databinding.FragmentStatsBinding
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private lateinit var binding: FragmentStatsBinding
    private lateinit var repository: TaskRepository
    private lateinit var recordsAdapter: CompletionRecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = TaskRepository(requireContext())

        setupRecyclerView()
        setupTabs()
        loadTodayRecords()
    }

    override fun onResume() {
        super.onResume()
        when (binding.tabLayout.selectedTabPosition) {
            0 -> loadTodayRecords()
            1 -> loadWeekRecords()
            2 -> loadMonthRecords()
            3 -> loadYearRecords()
        }
    }

    private fun setupRecyclerView() {
        recordsAdapter = CompletionRecordsAdapter(emptyList())

        binding.rvRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recordsAdapter
        }
    }

    private fun setupTabs() {
        val tabs = listOf("今日", "本周", "本月", "全年")

        tabs.forEach { title ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadTodayRecords()
                    1 -> loadWeekRecords()
                    2 -> loadMonthRecords()
                    3 -> loadYearRecords()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadTodayRecords() {
        lifecycleScope.launch {
            val records = repository.getTodayCompletionRecords()
            recordsAdapter.updateRecords(records)

            // 只显示统计卡片
            binding.layoutStats.visibility = View.VISIBLE
            binding.layoutTaskStats.visibility = View.GONE
            binding.tvRecordsTitle.visibility = View.GONE

            // 更新统计数据
            val totalCount = records.size
            val totalDuration = records.sumOf { it.duration }
            binding.tvTotalCount.text = totalCount.toString()
            binding.tvTotalDuration.text = totalDuration.toString()

            // 计算今日最常做的任务
            if (records.isNotEmpty()) {
                val taskCounts = records.groupingBy { it.taskName }.eachCount()
                val mostTask = taskCounts.maxByOrNull { it.value }

                if (mostTask != null && mostTask.value > 0) {
                    binding.cardTodayMostTask.visibility = View.VISIBLE
                    binding.tvTodayMostTask.text = mostTask.key
                    binding.tvTodayMostTaskCount.text = "完成 ${mostTask.value} 次"
                } else {
                    binding.cardTodayMostTask.visibility = View.GONE
                }
            } else {
                binding.cardTodayMostTask.visibility = View.GONE
            }

            if (records.isEmpty()) {
                binding.tvEmptyHint.visibility = View.VISIBLE
                binding.rvRecords.visibility = View.GONE
            } else {
                binding.tvEmptyHint.visibility = View.GONE
                binding.rvRecords.visibility = View.VISIBLE
            }
        }
    }

    private fun loadWeekRecords() {
        lifecycleScope.launch {
            val records = repository.getWeekCompletionRecords()
            recordsAdapter.updateRecords(records)

            binding.layoutStats.visibility = View.VISIBLE
            binding.layoutTaskStats.visibility = View.VISIBLE
            binding.tvRecordsTitle.visibility = View.VISIBLE

            val totalCount = records.size
            val totalDuration = records.sumOf { it.duration }
            binding.tvTotalCount.text = totalCount.toString()
            binding.tvTotalDuration.text = totalDuration.toString()

            // 获取所有任务,包括完成次数为0的任务
            val allTasks = repository.getAllTasks()
            val taskCounts = records.groupingBy { it.taskName }.eachCount().toMutableMap()

            // 为所有任务添加完成次数(默认为0)
            allTasks.forEach { task ->
                if (!taskCounts.containsKey(task.name)) {
                    taskCounts[task.name] = 0
                }
            }

            if (taskCounts.isNotEmpty()) {
                val mostTask = taskCounts.maxByOrNull { it.value }!!
                binding.tvMostTask.text = mostTask.key
                binding.tvMostTaskCount.text = "完成 ${mostTask.value} 次"

                val leastTask = taskCounts.minByOrNull { it.value }!!
                binding.tvLeastTask.text = leastTask.key
                binding.tvLeastTaskCount.text = "完成 ${leastTask.value} 次"
            }

            if (records.isEmpty()) {
                binding.tvEmptyHint.visibility = View.VISIBLE
                binding.rvRecords.visibility = View.GONE
            } else {
                binding.tvEmptyHint.visibility = View.GONE
                binding.rvRecords.visibility = View.VISIBLE
            }
        }
    }

    private fun loadMonthRecords() {
        lifecycleScope.launch {
            val records = repository.getMonthCompletionRecords()
            recordsAdapter.updateRecords(records)

            binding.layoutStats.visibility = View.VISIBLE
            binding.layoutTaskStats.visibility = View.VISIBLE
            binding.tvRecordsTitle.visibility = View.VISIBLE

            val totalCount = records.size
            val totalDuration = records.sumOf { it.duration }
            binding.tvTotalCount.text = totalCount.toString()
            binding.tvTotalDuration.text = totalDuration.toString()

            // 获取所有任务,包括完成次数为0的任务
            val allTasks = repository.getAllTasks()
            val taskCounts = records.groupingBy { it.taskName }.eachCount().toMutableMap()

            // 为所有任务添加完成次数(默认为0)
            allTasks.forEach { task ->
                if (!taskCounts.containsKey(task.name)) {
                    taskCounts[task.name] = 0
                }
            }

            if (taskCounts.isNotEmpty()) {
                val mostTask = taskCounts.maxByOrNull { it.value }!!
                binding.tvMostTask.text = mostTask.key
                binding.tvMostTaskCount.text = "完成 ${mostTask.value} 次"

                val leastTask = taskCounts.minByOrNull { it.value }!!
                binding.tvLeastTask.text = leastTask.key
                binding.tvLeastTaskCount.text = "完成 ${leastTask.value} 次"
            }

            if (records.isEmpty()) {
                binding.tvEmptyHint.visibility = View.VISIBLE
                binding.rvRecords.visibility = View.GONE
            } else {
                binding.tvEmptyHint.visibility = View.GONE
                binding.rvRecords.visibility = View.VISIBLE
            }
        }
    }

    private fun loadYearRecords() {
        lifecycleScope.launch {
            val records = repository.getYearCompletionRecords()
            recordsAdapter.updateRecords(records)

            binding.layoutStats.visibility = View.VISIBLE
            binding.layoutTaskStats.visibility = View.VISIBLE
            binding.tvRecordsTitle.visibility = View.VISIBLE

            val totalCount = records.size
            val totalDuration = records.sumOf { it.duration }
            binding.tvTotalCount.text = totalCount.toString()
            binding.tvTotalDuration.text = totalDuration.toString()

            // 获取所有任务,包括完成次数为0的任务
            val allTasks = repository.getAllTasks()
            val taskCounts = records.groupingBy { it.taskName }.eachCount().toMutableMap()

            // 为所有任务添加完成次数(默认为0)
            allTasks.forEach { task ->
                if (!taskCounts.containsKey(task.name)) {
                    taskCounts[task.name] = 0
                }
            }

            if (taskCounts.isNotEmpty()) {
                val mostTask = taskCounts.maxByOrNull { it.value }!!
                binding.tvMostTask.text = mostTask.key
                binding.tvMostTaskCount.text = "完成 ${mostTask.value} 次"

                val leastTask = taskCounts.minByOrNull { it.value }!!
                binding.tvLeastTask.text = leastTask.key
                binding.tvLeastTaskCount.text = "完成 ${leastTask.value} 次"
            }

            if (records.isEmpty()) {
                binding.tvEmptyHint.visibility = View.VISIBLE
                binding.rvRecords.visibility = View.GONE
            } else {
                binding.tvEmptyHint.visibility = View.GONE
                binding.rvRecords.visibility = View.VISIBLE
            }
        }
    }
}
