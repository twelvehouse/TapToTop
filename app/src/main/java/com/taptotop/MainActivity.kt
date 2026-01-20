package com.taptotop

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_open_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btn_battery_settings).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }

        setupSeekBar(R.id.seek_repeat_count, R.id.tv_repeat_count, "repeat_count", 2, 1)
        setupSeekBar(R.id.seek_duration, R.id.tv_duration, "duration", 20, 10)
    }

    private fun setupSeekBar(seekId: Int, tvId: Int, prefKey: String, default: Int, min: Int) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val seekBar = findViewById<SeekBar>(seekId)
        val textView = findViewById<TextView>(tvId)
        val current = prefs.getInt(prefKey, default)
        
        seekBar.progress = current
        textView.text = current.toString()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) {
                val value = if (p < min) min else p
                textView.text = value.toString()
                prefs.edit { putInt(prefKey, value) }
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val layoutError = findViewById<LinearLayout>(R.id.layout_error)
        layoutError.visibility = if (isAccessibilityServiceEnabled()) View.GONE else View.VISIBLE
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, TapToTopService::class.java)
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            val component = ComponentName.unflattenFromString(splitter.next())
            if (component != null && component == expected) return true
        }
        return false
    }
}
