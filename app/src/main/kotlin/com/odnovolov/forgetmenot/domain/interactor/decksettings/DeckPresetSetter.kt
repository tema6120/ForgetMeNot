package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference

class DeckPresetSetter {
    private var restore: (() -> Unit)? = null

    fun setDeckPreset(decks: List<Deck>, exercisePreference: ExercisePreference): Int {
        val backup = ArrayList<Pair<Deck, ExercisePreference>>()
        for (deck: Deck in decks) {
            if (deck.exercisePreference.id != exercisePreference.id) {
                backup.add(deck to deck.exercisePreference)
                deck.exercisePreference = exercisePreference
            }
        }
        if (backup.isEmpty()) return 0
        restore = {
            backup.forEach { (deck: Deck, exercisePreference: ExercisePreference) ->
                deck.exercisePreference = exercisePreference
            }
        }
        return backup.size
    }

    fun cancel() {
        restore?.invoke()
        restore = null
    }
}