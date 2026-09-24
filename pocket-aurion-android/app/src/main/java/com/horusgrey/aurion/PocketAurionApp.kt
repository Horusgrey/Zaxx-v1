package com.horusgrey.aurion

import android.app.Application
import com.horusgrey.aurion.core.AurionRepository

class PocketAurionApp : Application() {
    /**
     * Lazy so the resident `:interactor` process (AurionVoiceInteractionService) never opens the
     * database or loads the native runtime; only the UI/session process touches this.
     */
    val aurion: AurionRepository by lazy { AurionRepository(this) }
}
