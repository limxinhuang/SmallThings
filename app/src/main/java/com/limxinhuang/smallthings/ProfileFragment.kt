package com.limxinhuang.smallthings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.limxinhuang.smallthings.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var repository: TaskRepository

    // 用于选择文件导入
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val jsonString = inputStream?.bufferedReader().use { it?.readText() }

                if (jsonString != null) {
                    lifecycleScope.launch {
                        try {
                            repository.importDataFromJson(jsonString)
                            Toast.makeText(requireContext(), "数据恢复成功!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "恢复失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "文件读取失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "文件读取失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = TaskRepository(requireContext())

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.layoutBackup.setOnClickListener {
            showBackupDialog()
        }

        binding.layoutRestore.setOnClickListener {
            showRestoreDialog()
        }
    }

    private fun showBackupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("备份数据")
            .setMessage("将导出所有任务和完成记录为 JSON 文件。\n\n文件将保存在 Download 文件夹中。")
            .setPositiveButton("备份") { _, _ ->
                backupData()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showRestoreDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("恢复数据")
            .setMessage("警告: 恢复数据将**清空当前所有数据**!\n\n请选择之前备份的 JSON 文件。")
            .setPositiveButton("选择文件") { _, _ ->
                pickFileLauncher.launch("*/*")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun backupData() {
        lifecycleScope.launch {
            try {
                val jsonString = repository.exportDataToJson()

                // 生成文件名: smallthings_backup_20250113_153045.json
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "smallthings_backup_${dateFormat.format(Date())}.json"

                // 保存到 Download 文件夹
                val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )

                val file = java.io.File(downloadDir, fileName)

                try {
                    FileOutputStream(file).use { output ->
                        output.write(jsonString.toByteArray(Charsets.UTF_8))
                    }

                    Toast.makeText(
                        requireContext(),
                        "备份成功!\n文件: ${file.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()

                    // 延迟分享,避免与Toast冲突
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        shareBackupFile(file)
                    }, 500)

                } catch (e: IOException) {
                    Toast.makeText(
                        requireContext(),
                        "保存失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "备份失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareBackupFile(file: java.io.File) {
        try {
            // 使用 FileProvider 创建 URI
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "分享备份文件"))
        } catch (e: Exception) {
            // 分享失败不影响备份,只显示日志
            e.printStackTrace()
        }
    }
}
