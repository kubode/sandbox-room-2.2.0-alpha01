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
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.airbnb.epoxy.EpoxyRecyclerView

@Database(
    entities = [
        Project::class
    ],
    version = 1
)
@TypeConverters(CharSeqConv::class)
abstract class TargetEntityDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}

class CharSeqConv {
    @TypeConverter
    fun fromString(value: String?): CharSequence? = value

    @TypeConverter
    fun toString(value: CharSequence?): String? = value?.toString()
}

@Entity
data class Project(
    @PrimaryKey
    val id: Long,
    val title: String,
    @ColumnInfo(defaultValue = "''")
    val longDescription: String
)

// Using val will cause "Cannot find setter for field" compile error.
// https://issuetracker.google.com/issues/138664463
data class ProjectMiniApiEntity(
    var id: Long,
    @ColumnInfo(name = "title") // Map to Entity's column
    var title2: CharSequence // Converted to Entity's column using TypeConverter
)

@Dao
interface ProjectDao {
    @Insert
    fun insertNewProject(project: Project)

    @Insert(entity = Project::class)
    fun insertNewProject(projectMini: ProjectMiniApiEntity)

    @Update
    fun updateProject(project: Project)

    @Update(entity = Project::class)
    fun updateProject(projectMini: ProjectMiniApiEntity)

    @Query("""SELECT * FROM Project""")
    fun loadAllProjects(): LiveData<List<Project>>
}

class TargetEntityFragment : Fragment() {
    private val db: TargetEntityDatabase by lazy {
        Room.inMemoryDatabaseBuilder(requireContext(), TargetEntityDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
    private val dao: ProjectDao by lazy { db.projectDao() }

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
            dao.loadAllProjects().observe(viewLifecycleOwner) { projects ->
                withModels {
                    projects.forEach { project ->
                        simpleTextItemView {
                            id(project.id)
                            number(project.id)
                            text(
                                """
                                    Title: ${project.title}
                                    Long Description: ${project.longDescription}
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
                    val id = System.currentTimeMillis()
                    dao.insertNewProject(
                        ProjectMiniApiEntity(
                            id = id,
                            title2 = "Title of $id"
                        )
                    )
                }
            }
        }
    }
}
