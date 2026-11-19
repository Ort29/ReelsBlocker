package com.example.reelsblocker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

object Prefs {
    private const val FILE = "prefs"
    private const val KEY = "enabled"
    fun isEnabled(c: Context) = c.getSharedPreferences(FILE, 0).getBoolean(KEY, true)
    fun setEnabled(c: Context, v: Boolean) =
        c.getSharedPreferences(FILE, 0).edit().putBoolean(KEY, v).apply()
}

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (24 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 26f
            gravity = Gravity.CENTER
        }

        val toggle = Switch(this).apply {
            text = "حجب الريلز والشورتس"
            textSize = 18f
            isChecked = Prefs.isEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, on -> Prefs.setEnabled(this@MainActivity, on) }
        }

        status = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, pad, 0, pad)
        }

        val open = Button(this).apply {
            text = "فتح إعدادات الوصول"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(toggle)
        layout.addView(status)
        layout.addView(open)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        status.text = if (isServiceEnabled())
            "الخدمة مفعّلة في النظام ✅"
        else
            "الخدمة غير مفعّلة. افتح إعدادات الوصول وفعّل «${getString(R.string.app_name)}»."
    }

    private fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(packageName)
    }
}
