package com.limxinhuang.smallthings

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.limxinhuang.smallthings.databinding.ActivityTaskExecutionBinding
import java.util.Locale

class TaskExecutionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskExecutionBinding
    private lateinit var dbHelper: TaskDatabaseHelper
    private var taskId: Long = 0
    private var taskName: String = ""
    private var taskColor: String = ""
    private var durationMinutes: Int = 0

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskExecutionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = TaskDatabaseHelper(this)

        taskId = intent.getLongExtra("task_id", 0)
        taskName = intent.getStringExtra("task_name") ?: ""
        taskColor = intent.getStringExtra("task_color") ?: "#FF5252"
        durationMinutes = intent.getIntExtra("task_duration", 15)

        setupToolbar()
        setupUI()
        setupButtons()
        startCountdown()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "放弃"
        binding.toolbar.setNavigationOnClickListener {
            showAbandonDialog()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showAbandonDialog()
    }

    private fun setupUI() {
        // 设置背景为白色
        binding.rootLayout.setBackgroundColor(Color.WHITE)

        // 设置任务名称和颜色
        binding.tvTaskName.text = taskName
        try {
            val color = Color.parseColor(taskColor)
            binding.tvTaskName.setTextColor(color)
        } catch (e: Exception) {
            binding.tvTaskName.setTextColor(Color.parseColor("#FF5252"))
        }

        // 设置其他文字为黑色
        binding.tvCountdown.setTextColor(Color.BLACK)
        binding.tvFocusingLabel.setTextColor(Color.BLACK)
        binding.tvFocusingLabel.alpha = 0.7f

        // 设置按钮颜色为紫色
        binding.btnCompleteEarly.setBackgroundColor(Color.parseColor("#6200EE"))
    }

    private fun isColorDark(colorHex: String): Boolean {
        val color = Color.parseColor(colorHex)
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun setupButtons() {
        binding.btnCompleteEarly.setOnClickListener {
            completeTask()
        }
    }

    private fun startCountdown() {
        timeLeftInMillis = durationMinutes * 60 * 1000L

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                updateCountdownText()
                completeTask()
            }
        }.start()
    }

    private fun updateCountdownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvCountdown.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun completeTask() {
        countDownTimer?.cancel()

        // 增加完成次数
        dbHelper.incrementCompletedCount(taskId)

        // 插入完成记录
        dbHelper.insertCompletionRecord(taskId, taskName, taskColor, durationMinutes)

        Toast.makeText(this, "恭喜！完成了任务", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showAbandonDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("放弃任务")
            .setMessage("确定要放弃这个任务吗？")
            .setPositiveButton("放弃") { _, _ ->
                abandonTask()
            }
            .setNegativeButton("继续", null)
            .show()
    }

    private fun abandonTask() {
        countDownTimer?.cancel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
