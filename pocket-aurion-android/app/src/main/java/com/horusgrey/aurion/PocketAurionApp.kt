package com.horusgrey.aurion

import android.app.Application
import com.horusgrey.aurion.core.AurionRepository

class PocketAurionApp : Application() {
    lateinit var aurion: AurionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        aurion = AurionRepository(this)
    }
}
