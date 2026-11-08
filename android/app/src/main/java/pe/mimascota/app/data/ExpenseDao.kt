package pe.mimascota.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE petId = :petId ORDER BY date DESC")
    fun observe(petId: String): Flow<List<ExpenseEntity>>
    @Insert suspend fun insert(item: ExpenseEntity)
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: String)
}
