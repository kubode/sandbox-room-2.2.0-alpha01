package com.github.kubode.sandboxroom22

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.airbnb.epoxy.EpoxyRecyclerView

@Database(entities = [PrePackaged::class], version = 1)
abstract class PrePackagedDatabase : RoomDatabase() {
    abstract fun prePackagedDao(): PrePackagedDao
}

@Entity
data class PrePackaged(
    @PrimaryKey
    val id: Long,
    val text: String
)

@Dao
interface PrePackagedDao {
    @Query("""SELECT * FROM PrePackaged""")
    fun loadAllPrePackaged(): List<PrePackaged>
}

class PrePackagedDatabaseFragment : Fragment() {
    private val db: PrePackagedDatabase by lazy {
        Room.databaseBuilder(requireContext(), PrePackagedDatabase::class.java, "pre-packaged.db")
            .createFromAsset("pre-packaged.db")
            .allowMainThreadQueries()
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return EpoxyRecyclerView(requireContext()).apply {
            withModels {
                db.prePackagedDao().loadAllPrePackaged().forEach { prePackaged ->
                    simpleTextItemView {
                        id(prePackaged.id)
                        number(prePackaged.id)
                        text(prePackaged.text)
                    }
                }
            }
        }
    }
}
