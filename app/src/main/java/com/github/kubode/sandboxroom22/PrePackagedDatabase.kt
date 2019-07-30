package com.github.kubode.sandboxroom22

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
    private val dao: PrePackagedDao by lazy {
        // createFromAsset cannot use with inMemoryDb
        Room.databaseBuilder(requireContext(), PrePackagedDatabase::class.java, "pre-packaged.db")
            .createFromAsset("pre-packaged.db")
            .allowMainThreadQueries()
            .build()
            .prePackagedDao()
    }

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
            withModels {
                dao.loadAllPrePackaged().forEach { prePackaged ->
                    simpleTextItemView {
                        id(prePackaged.id)
                        number(prePackaged.id)
                        text(prePackaged.text)
                    }
                }
            }
        }
        view.findViewById<View>(R.id.add).apply {
            isVisible = false
        }
    }
}
