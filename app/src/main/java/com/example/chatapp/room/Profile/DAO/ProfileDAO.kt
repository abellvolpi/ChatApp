package com.example.chatapp.room.Profile.DAO

import androidx.room.*
import com.example.chatapp.models.Profile

@Dao
interface ProfileDAO {
    @Query("Select * from Profile")
    fun getAll(): List<Profile>

    @Insert
    fun insert(vararg profile: Profile)

    @Delete
    fun delete(vararg profile: Profile)

    @Update
    fun update(vararg profile: Profile)

    @Query("Select * from Profile where id = :id")
    fun getById(vararg id: String): Profile?

    @Query("Delete From Profile")
    fun deleteAll()
}