package com.example.chatapp.room.profile.dao

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

    @Query("Select * from Profile where ipAddress = :ip")
    fun getByIpAddress(vararg ip: String): Profile?

    @Query("Delete From Profile")
    fun deleteAll()
}