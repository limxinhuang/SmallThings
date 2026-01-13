package com.limxinhuang.smallthings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limxinhuang.smallthings.databinding.ItemCompletionRecordBinding

class CompletionRecordsAdapter(
    private var records: List<CompletionRecord>
) : RecyclerView.Adapter<CompletionRecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletionRecordViewHolder {
        val binding = ItemCompletionRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompletionRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompletionRecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<CompletionRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
