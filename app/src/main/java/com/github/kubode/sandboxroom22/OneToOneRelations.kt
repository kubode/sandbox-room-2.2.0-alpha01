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
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.airbnb.epoxy.EpoxyRecyclerView

@Database(
    entities = [
        User::class,
        Profile::class
    ],
    version = 1
)
abstract class OneToOneRelationsDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Entity
data class User(
    @PrimaryKey
    val userId: Long,
    val name: String
)

@Entity
data class Profile(
    @PrimaryKey
    val userId: Long,
    val selfIntroduction: String
)

data class UserWithProfile(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = "userId",
        entity = Profile::class,
        entityColumn = "userId"
    )
    val profile: Profile?
)

@Dao
interface UserDao {

    @Insert
    fun insertUser(user: User)

    @Insert
    fun insertProfile(profile: Profile)

    @Transaction
    @Query("""SELECT * FROM User""")
    fun loadAllUserWithProfile(): LiveData<List<UserWithProfile>>
}

class OneToOneRelationsFragment : Fragment() {
    private val db: OneToOneRelationsDatabase by lazy {
        Room.inMemoryDatabaseBuilder(requireContext(), OneToOneRelationsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
    private val dao: UserDao by lazy { db.userDao() }

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
            dao.loadAllUserWithProfile().observe(viewLifecycleOwner) { records ->
                withModels {
                    records.forEach { record ->
                        simpleTextItemView {
                            id(record.user.userId)
                            number(record.user.userId)
                            text(
                                """
                                    Name: ${record.user.name}
                                    Self Introduction: ${record.profile?.selfIntroduction}
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
                    val user = User(id, "Name of $id")
                    dao.insertUser(user)
                    if (id % 2 == 0L) {
                        dao.insertProfile(Profile(id, "Self introduction of $id"))
                    }
                }
            }
        }
    }
}
