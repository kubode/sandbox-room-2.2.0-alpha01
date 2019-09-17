package com.github.kubode.sandboxroom22

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.room.*
import com.airbnb.epoxy.EpoxyRecyclerView

@Database(
    entities = [
        Project::class
    ],
    version = 1
)
@TypeConverters(
    CompanyApiEntityConverter::class
)
abstract class TargetEntityDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}

class CompanyApiEntityConverter {
    @TypeConverter
    fun toCompanyId(value: CompanyMiniApiEntity?): Long? = value?.id
}

// DB
@Entity
data class Project(
    @PrimaryKey
    val id: Long,
    val title: String,
    @ColumnInfo(defaultValue = "''")
    val longDescription: String,
    @Embedded(prefix = "avatar_")
    val avatar: Avatar,
    val companyId: Long? // Foreign Key
)

data class Avatar(
    val url: String,
    val captionText: String
)

// API
data class ProjectMiniApiEntity(
    val id: Long,
    @ColumnInfo(name = "title") // Map to Entity's column
    val title2: String,
    @Ignore
    val list: List<String>,
    @Embedded(prefix = "avatar_") // Spread all of properties using prefix
    val avatar: AvatarApiEntity,
    @ColumnInfo(name = "companyId") // Converted to Entity's column using TypeConverter
    val company: CompanyMiniApiEntity?
)

data class AvatarApiEntity(
    val url: String,
    @ColumnInfo(name = "captionText")
    val caption_text: String
)

data class CompanyMiniApiEntity(
    val id: Long,
    val name: String
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
                            text(project.toString())
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
                            title2 = "Title of $id",
                            list = emptyList(),
                            avatar = AvatarApiEntity("foo", "test"),
                            company = CompanyMiniApiEntity(1L, "wantedly")
                        )
                    )
                }
            }
        }
    }
}
