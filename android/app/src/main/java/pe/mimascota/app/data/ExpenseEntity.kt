package pe.mimascota.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expenses", foreignKeys = [ForeignKey(entity = PetEntity::class, parentColumns = ["id"], childColumns = ["petId"], onDelete = ForeignKey.CASCADE)], indices = [Index("petId")])
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val title: String,
    val amountCents: Long,
    val date: String
)
