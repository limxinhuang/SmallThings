package com.limxinhuang.smallthings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.limxinhuang.smallthings.databinding.FragmentDailySummaryBinding
import kotlinx.coroutines.launch

class DailySummaryFragment : Fragment() {

    private lateinit var binding: FragmentDailySummaryBinding
    private lateinit var repository: TaskRepository
    private lateinit var adapter: DailySummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDailySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = TaskRepository(requireContext())

        setupRecyclerView()
        loadSummaries()
    }

    override fun onResume() {
        super.onResume()
        loadSummaries()
    }

    private fun setupRecyclerView() {
        adapter = DailySummaryAdapter(emptyList())

        binding.rvSummaries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DailySummaryFragment.adapter
        }
    }

    private fun loadSummaries() {
        lifecycleScope.launch {
            val summaries = repository.getAllDailySummaries()
            adapter.updateSummaries(summaries)

            // 显示或隐藏空状态
            if (summaries.isEmpty()) {
                binding.tvEmptyHint.visibility = View.VISIBLE
                binding.rvSummaries.visibility = View.GONE
            } else {
                binding.tvEmptyHint.visibility = View.GONE
                binding.rvSummaries.visibility = View.VISIBLE
            }
        }
    }
}
