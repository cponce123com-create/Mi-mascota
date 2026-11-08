package pe.mimascota.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "vaccines", foreignKeys = [ForeignKey(entity = PetEntity::class, parentColumns = ["id"], childColumns = ["petId"], onDelete = ForeignKey.CASCADE)], indices = [Index("petId")])
data class VaccineEntity(@PrimaryKey val id: String, val petId: String, val name: String, val appliedAt: String?, val dueAt: String?)

@Entity(tableName = "reminders", foreignKeys = [ForeignKey(entity = PetEntity::class, parentColumns = ["id"], childColumns = ["petId"], onDelete = ForeignKey.CASCADE)], indices = [Index("petId")])
data class ReminderEntity(@PrimaryKey val id: String, val petId: String, val title: String, val dueAt: String, val completed: Boolean = false)

@Entity(tableName = "photos", foreignKeys = [ForeignKey(entity = PetEntity::class, parentColumns = ["id"], childColumns = ["petId"], onDelete = ForeignKey.CASCADE)], indices = [Index("petId")])
data class PhotoEntity(@PrimaryKey val id: String, val petId: String, val uri: String, val createdAt: Long)
