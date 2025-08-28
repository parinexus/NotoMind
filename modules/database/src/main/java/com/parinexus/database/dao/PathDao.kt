package com.parinexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.parinexus.database.model.DrawPathEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PathDao {
    @Query("SELECT * FROM path_table WHERE image_id =:imageID ORDER BY id")
    fun getPaths(imageID: Long): Flow<List<DrawPathEntity>>

    @Query("DELETE FROM PATH_TABLE WHERE image_id =:imageID")
    fun delete(imageID: Long)

    @Insert
    fun insert(list: List<DrawPathEntity>)
}
