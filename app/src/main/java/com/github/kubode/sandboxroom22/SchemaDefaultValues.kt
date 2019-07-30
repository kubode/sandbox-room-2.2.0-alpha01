package com.github.kubode.sandboxroom22

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.airbnb.epoxy.EpoxyRecyclerView
import java.util.Date

@Database(entities = [SchemaDefaultValues::class], version = 1)
abstract class SchemaDefaultValuesDatabase : RoomDatabase() {
    abstract fun schemaDefaultValuesDao(): SchemaDefaultValuesDao
}

@Entity
data class SchemaDefaultValues(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val text: String,
    @ColumnInfo(defaultValue = "('Created at ' || CURRENT_TIMESTAMP)")
    val defaultValue: String
)

@Dao
interface SchemaDefaultValuesDao {
    @Query("""SELECT * FROM SchemaDefaultValues""")
    fun loadAllSchemaDefaultValues(): LiveData<List<SchemaDefaultValues>>

    // defaultValue cannot use with @Insert
    @Query("""INSERT INTO SchemaDefaultValues (text) VALUES (:text)""")
    fun insertNewRecord(text: String)
}

class SchemaDefaultValuesFragment : Fragment() {
    private val dao: SchemaDefaultValuesDao by lazy {
        Room.inMemoryDatabaseBuilder(requireContext(), SchemaDefaultValuesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            .schemaDefaultValuesDao()
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
            dao.loadAllSchemaDefaultValues().observe(viewLifecycleOwner) { records ->
                withModels {
                    records.forEach { record ->
                        simpleTextItemView {
                            id(record.id)
                            number(record.id)
                            text(record.toString())
                        }
                    }
                }
            }
        }
        view.findViewById<View>(R.id.add).apply {
            setOnClickListener {
                dao.insertNewRecord(Date().toString())
            }
        }
    }
}
