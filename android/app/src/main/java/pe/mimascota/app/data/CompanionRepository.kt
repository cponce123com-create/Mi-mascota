package pe.mimascota.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.virtualCompanion by preferencesDataStore(name = "virtual_companion")
private val ENERGY = intPreferencesKey("energy")
private val HAPPINESS = intPreferencesKey("happiness")
private val PLAYS = intPreferencesKey("plays")
private val SCORE = intPreferencesKey("score")

data class CompanionStats(
    val energy: Int = 70,
    val happiness: Int = 70,
    val plays: Int = 0,
    val score: Int = 0
)

object CompanionRepository {
    fun observe(context: Context): Flow<CompanionStats> = context.virtualCompanion.data.map { prefs ->
        CompanionStats(prefs[ENERGY] ?: 70, prefs[HAPPINESS] ?: 70,
            prefs[PLAYS] ?: 0, prefs[SCORE] ?: 0)
    }
    suspend fun reset(context: Context) {
        context.virtualCompanion.edit {
            it.remove(ENERGY)
            it.remove(HAPPINESS)
            it.remove(PLAYS)
            it.remove(SCORE)
        }
    }

    suspend fun rest(context: Context) {
        context.virtualCompanion.edit {
            it[ENERGY] = ((it[ENERGY] ?: 70) + 20).coerceAtMost(100)
            it[HAPPINESS] = ((it[HAPPINESS] ?: 70) + 5).coerceAtMost(100)
        }
    }
    suspend fun play(context: Context) {
        context.virtualCompanion.edit {
            val energy = it[ENERGY] ?: 70
            if (energy >= 10) {
                it[ENERGY] = energy - 10
                it[HAPPINESS] = ((it[HAPPINESS] ?: 70) + 15).coerceAtMost(100)
                it[PLAYS] = (it[PLAYS] ?: 0) + 1
            }
        }
    }
    suspend fun guess(context: Context, correct: Boolean) {
        context.virtualCompanion.edit {
            it[PLAYS] = (it[PLAYS] ?: 0) + 1
            if (correct) {
                it[SCORE] = (it[SCORE] ?: 0) + 1
                it[HAPPINESS] = ((it[HAPPINESS] ?: 70) + 8).coerceAtMost(100)
            } else {
                it[ENERGY] = ((it[ENERGY] ?: 70) - 2).coerceAtLeast(0)
            }
        }
    }
}
