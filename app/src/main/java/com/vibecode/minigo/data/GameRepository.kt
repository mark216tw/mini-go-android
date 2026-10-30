package com.vibecode.minigo.data

import android.content.Context
import android.util.AtomicFile
import com.vibecode.minigo.engine.GameState
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SavedGame(
    val version: Int = 1,
    val config: GameConfig,
    val state: GameState,
)

interface GameStore {
    suspend fun save(saved: SavedGame)

    suspend fun load(): SavedGame?

    suspend fun clear()
}

class GameRepository private constructor(context: Context) : GameStore {

    private val file = AtomicFile(File(context.applicationContext.filesDir, FILE_NAME))
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun save(saved: SavedGame) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val bytes = json.encodeToString(saved).toByteArray(Charsets.UTF_8)
            val stream = file.startWrite()
            try {
                stream.write(bytes)
                file.finishWrite(stream)
            } catch (exception: IOException) {
                file.failWrite(stream)
                throw exception
            }
        }
    }

    override suspend fun load(): SavedGame? = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                json.decodeFromString<SavedGame>(file.readFully().toString(Charsets.UTF_8))
            }.getOrNull()
        }
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        mutex.withLock {
            file.delete()
        }
    }

    companion object {
        private const val FILE_NAME = "current_game.json"

        @Volatile
        private var instance: GameRepository? = null

        fun get(context: Context): GameRepository =
            instance ?: synchronized(this) {
                instance ?: GameRepository(context).also { instance = it }
            }
    }
}
