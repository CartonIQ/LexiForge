package com.cartoniq.lexiforge.data

import android.content.Context
import android.content.SharedPreferences

class AppPrefs private constructor(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("lexiforge_prefs", Context.MODE_PRIVATE)

    var seeded: Boolean
        get() = sp.getBoolean("seeded", false)
        set(value) {
            sp.edit().putBoolean("seeded", value).apply()
        }

    var reminderEnabled: Boolean
        get() = sp.getBoolean("reminder_enabled", false)
        set(value) {
            sp.edit().putBoolean("reminder_enabled", value).apply()
        }

    var reminderHour: Int
        get() = sp.getInt("reminder_hour", 19)
        set(value) {
            sp.edit().putInt("reminder_hour", value).apply()
        }

    var reminderMinute: Int
        get() = sp.getInt("reminder_minute", 0)
        set(value) {
            sp.edit().putInt("reminder_minute", value).apply()
        }

    var autoSpeak: Boolean
        get() = sp.getBoolean("auto_speak", false)
        set(value) {
            sp.edit().putBoolean("auto_speak", value).apply()
        }

    var haptics: Boolean
        get() = sp.getBoolean("haptics", true)
        set(value) {
            sp.edit().putBoolean("haptics", value).apply()
        }

    companion object {
        @Volatile
        private var instance: AppPrefs? = null

        fun get(context: Context): AppPrefs {
            return instance ?: synchronized(this) {
                instance ?: AppPrefs(context).also { instance = it }
            }
        }
    }
}
