package com.limxinhuang.smallthings

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.limxinhuang.smallthings.databinding.ActivityCreateTaskBinding
import android.widget.SeekBar
import com.limxinhuang.smallthings.R
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.GridView
import android.graphics.drawable.ColorDrawable
import android.widget.BaseAdapter
import kotlinx.coroutines.launch

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private var selectedColorIndex = 0
    private var selectedColor = AVAILABLE_COLORS[0]
    private lateinit var repository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = TaskRepository(this)

        setupToolbar()
        setupColorPicker()
        setupViews()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // 点击输入框外部时失焦并收起键盘
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
        supportActionBar?.title = "创建事项"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupColorPicker() {
        // 设置默认颜色
        updateColorPreview(selectedColor)

        // 点击选择颜色
        binding.llColorSelector.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun updateColorPreview(colorCode: String) {
        val color = Color.parseColor(colorCode)
        binding.viewColorPreview.setBackgroundColor(color)
    }

    private fun showColorPickerDialog() {
        lifecycleScope.launch {
            val dialogView = LayoutInflater.from(this@CreateTaskActivity).inflate(R.layout.dialog_color_picker, null)
            val gridView = dialogView.findViewById<GridView>(R.id.gv_colors)

            // 获取已使用的颜色，过滤出未使用的颜色
            val usedColors = repository.getUsedColors()
            val availableColors = AVAILABLE_COLORS.filterNot { it in usedColors }

            if (availableColors.isEmpty()) {
                Toast.makeText(this@CreateTaskActivity, "所有颜色都已使用，请先删除一些任务", Toast.LENGTH_LONG).show()
                return@launch
            }

            val adapter = ColorAdapter(availableColors)
            gridView.adapter = adapter

            val dialog = AlertDialog.Builder(this@CreateTaskActivity)
                .setView(dialogView)
                .create()

            gridView.setOnItemClickListener { _, _, position, _ ->
                selectedColor = availableColors[position]
                updateColorPreview(selectedColor)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    inner class ColorAdapter(private val colors: List<String>) : BaseAdapter() {
        override fun getCount(): Int = colors.size

        override fun getItem(position: Int): Any = colors[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@CreateTaskActivity)
                .inflate(R.layout.item_color, parent, false)

            val colorView = view.findViewById<View>(R.id.view_color)
            val color = Color.parseColor(colors[position])
            colorView.setBackgroundColor(color)

            return view
        }
    }

    private fun setupViews() {
        // 设置SeekBar监听器
        binding.seekbarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // 进度范围是0-29，对应1-30分钟
                val minutes = progress + 1
                binding.tvDuration.text = "$minutes 分钟"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 保存按钮点击事件
        binding.btnSave.setOnClickListener {
            val taskName = binding.etTaskName.text.toString()
            if (taskName.isBlank()) {
                Toast.makeText(this, "请输入事项名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val duration = binding.seekbarDuration.progress + 1

            // 保存到数据库
            lifecycleScope.launch {
                val task = Task(
                    name = taskName,
                    colorCode = selectedColor,
                    durationMinutes = duration
                )

                val id = repository.insertTask(task)
                if (id > 0) {
                    Toast.makeText(this@CreateTaskActivity, "创建成功: $taskName, $duration", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateTaskActivity, "创建失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
