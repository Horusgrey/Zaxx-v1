package com.horusgrey.aurion.assistant

import android.service.voice.VoiceInteractionService

/** Keep this service intentionally tiny; Android keeps the active voice interactor resident. */
class AurionVoiceInteractionService : VoiceInteractionService()
