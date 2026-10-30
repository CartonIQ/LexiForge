package com.cartoniq.lexiforge

import android.app.Application
import com.cartoniq.lexiforge.ads.AdManager
import com.cartoniq.lexiforge.data.AppDatabase
import com.cartoniq.lexiforge.data.AppPrefs
import com.cartoniq.lexiforge.data.LexiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LexiForgeApp : Application() {

    lateinit var repository: LexiRepository
        private set
    lateinit var adManager: AdManager
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = LexiRepository(database, AppPrefs.get(this))
        adManager = AdManager(this)
        adManager.initialize()
        appScope.launch { repository.initialize() }
    }
}

fun Application.lexiApp(): LexiForgeApp = this as LexiForgeApp
