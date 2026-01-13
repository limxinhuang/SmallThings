package com.limxinhuang.smallthings

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.limxinhuang.smallthings.databinding.ActivityEditTaskBinding
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.GridView
import android.widget.BaseAdapter

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding
    private lateinit var dbHelper: TaskDatabaseHelper
    private var taskId: Long = 0

    private val colorOptions = listOf(
        ColorOption("#FF5252"),
        ColorOption("#FF9800"),
        ColorOption("#FFEB3B"),
        ColorOption("#4CAF50"),
        ColorOption("#69F0AE"),
        ColorOption("#2196F3"),
        ColorOption("#0D47A1"),
        ColorOption("#9C27B0"),
        ColorOption("#673AB7"),
        ColorOption("#E91E63"),
        ColorOption("#FF4081"),
        ColorOption("#CDDC39"),
        ColorOption("#00BCD4"),
        ColorOption("#009688"),
        ColorOption("#3F51B5"),
        ColorOption("#D32F2F"),
        ColorOption("#FFC107"),
        ColorOption("#FF5722"),
        ColorOption("#8BC34A"),
        ColorOption("#607D8B")
    )
    private var selectedColorIndex = 0
    private var selectedColor = colorOptions[0].colorCode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = TaskDatabaseHelper(this)

        taskId = intent.getLongExtra("task_id", 0)

        setupToolbar()
        setupBackPressedCallback()
        setupColorPicker()
        loadTask()
        setupViews()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveAndFinish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view != null) {
                val rect = android.graphics.Rect()
                view.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    view.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "编辑事项"
        binding.toolbar.setNavigationOnClickListener {
            saveAndFinish()
        }
    }

    private fun saveAndFinish() {
        val taskName = binding.etTaskName.text.toString()
        if (taskName.isBlank()) {
            Toast.makeText(this, "请输入事项名称", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = binding.seekbarDuration.progress + 1

        val task = Task(
            id = taskId,
            name = taskName,
            colorCode = selectedColor,
            durationMinutes = duration
        )

        val updated = dbHelper.updateTask(task)
        if (updated) {
            finish()
        } else {
            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupColorPicker() {
        updateColorPreview(selectedColor)

        binding.llColorSelector.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun updateColorPreview(colorCode: String) {
        val color = Color.parseColor(colorCode)
        binding.viewColorPreview.setBackgroundColor(color)
    }

    private fun showColorPickerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null)
        val gridView = dialogView.findViewById<GridView>(R.id.gv_colors)

        val adapter = ColorAdapter()
        gridView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedColorIndex = position
            selectedColor = colorOptions[position].colorCode
            updateColorPreview(selectedColor)
            dialog.dismiss()
        }

        dialog.show()
    }

    inner class ColorAdapter : BaseAdapter() {
        override fun getCount(): Int = colorOptions.size

        override fun getItem(position: Int): Any = colorOptions[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup?): android.view.View {
            val view = convertView ?: LayoutInflater.from(this@EditTaskActivity)
                .inflate(R.layout.item_color, parent, false)

            val colorView = view.findViewById<android.view.View>(R.id.view_color)
            val color = Color.parseColor(colorOptions[position].colorCode)
            colorView.setBackgroundColor(color)

            return view
        }
    }

    private fun loadTask() {
        val tasks = dbHelper.getAllTasks()
        val task = tasks.find { it.id == taskId }

        task?.let {
            binding.etTaskName.setText(it.name)
            binding.seekbarDuration.progress = it.durationMinutes - 1
            binding.tvDuration.text = "${it.durationMinutes} 分钟"

            selectedColor = it.colorCode
            selectedColorIndex = colorOptions.indexOfFirst { it.colorCode == selectedColor }
            if (selectedColorIndex == -1) selectedColorIndex = 0
            updateColorPreview(selectedColor)
        }
    }

    private fun setupViews() {
        binding.seekbarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = progress + 1
                binding.tvDuration.text = "$minutes 分钟"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("删除事项")
            .setMessage("确定要删除这个事项吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteTask()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteTask() {
        val deleted = dbHelper.deleteTask(taskId)
        if (deleted) {
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
        }
    }
}
