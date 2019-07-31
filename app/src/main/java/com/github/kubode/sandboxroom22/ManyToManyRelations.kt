package com.github.kubode.sandboxroom22

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.airbnb.epoxy.EpoxyRecyclerView
import java.util.Date
import kotlin.random.Random

@Database(
    entities = [
        Playlist::class,
        Song::class,
        PlaylistSongRef::class
    ],
    version = 1
)
abstract class ManyToManyRelationsDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}

@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long,
    val title: String
)

@Entity
data class Song(
    @PrimaryKey(autoGenerate = true)
    val songId: Long,
    val title: String
) {
    override fun toString() = "$songId: $title"
}

@Entity(primaryKeys = ["playlistId", "songId"])
data class PlaylistSongRef(
    val playlistId: Long,
    val songId: Long
)

data class PlaylistWithSongs(
    @Embedded
    val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entity = Song::class,
        entityColumn = "songId",
        associateBy = Junction(PlaylistSongRef::class)
    )
    val songs: List<Song>
)

@Dao
interface MusicDao {

    @Query("""INSERT INTO Song (title) VALUES (:title)""")
    fun addSong(title: String): Long

    @Query("""INSERT INTO Playlist (title) VALUES (:title)""")
    fun addPlaylist(title: String): Long

    @Insert
    fun addPlaylistSongRelation(vararg playlistSongRef: PlaylistSongRef)

    // If not annotate with @Transaction, Room compiler will output warnings.
    // Since Dao implementation runs another query for fetching relations.
    @Transaction
    @Query("""SELECT * FROM Playlist""")
    fun loadAllPlaylistWithSongs(): LiveData<List<PlaylistWithSongs>>
}

class ManyToManyRelationsFragment : Fragment() {
    private val db: ManyToManyRelationsDatabase by lazy {
        Room.inMemoryDatabaseBuilder(requireContext(), ManyToManyRelationsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
    private val dao: MusicDao by lazy { db.musicDao() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_common, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<EpoxyRecyclerView>(R.id.epoxy_recycler_view).apply {
            dao.loadAllPlaylistWithSongs().observe(viewLifecycleOwner) { records ->
                withModels {
                    records.forEach { record ->
                        simpleTextItemView {
                            id(record.playlist.playlistId)
                            number(record.playlist.playlistId)
                            text(
                                """
                                    Title: ${record.playlist.title}
                                    Songs: ${record.songs.joinToString()}
                                """.trimIndent()
                            )
                        }
                    }
                }
            }
        }
        view.findViewById<View>(R.id.add).apply {
            setOnClickListener {
                db.runInTransaction {
                    val songIds = mutableListOf<Long>()
                    dao.addPlaylist("UNDERTALE").let { playlistId ->
                        dao.addSong("SAVE the World").let { songId ->
                            dao.addPlaylistSongRelation(PlaylistSongRef(playlistId, songId))
                            songIds += songId
                        }
                        dao.addSong("MEGALOVANIA").let { songId ->
                            dao.addPlaylistSongRelation(PlaylistSongRef(playlistId, songId))
                            songIds += songId
                        }
                    }
                    dao.addPlaylist("DELTARUNE").let { playlistId ->
                        dao.addSong("THE WORLD REVOLVING").let { songId ->
                            dao.addPlaylistSongRelation(PlaylistSongRef(playlistId, songId))
                            songIds += songId
                        }
                        dao.addSong("Don't Forget").let { songId ->
                            dao.addPlaylistSongRelation(PlaylistSongRef(playlistId, songId))
                            songIds += songId
                        }
                    }
                    dao.addPlaylist("Toby Fox").let { playlistId ->
                        songIds.forEach { songId ->
                            dao.addPlaylistSongRelation(PlaylistSongRef(playlistId, songId))
                        }
                    }
                }
            }
        }
    }
}
