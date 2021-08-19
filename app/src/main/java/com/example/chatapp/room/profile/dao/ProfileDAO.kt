package com.example.chatapp.room.profile.dao

import androidx.room.*
import com.example.chatapp.models.Profile

@Dao
interface ProfileDAO {
    @Query("Select * from Profile")
    fun getAll(): List<Profile>

    @Insert
    fun insert(vararg profile: Profile)

    @Query("Delete from Profile where Profile.id = :id")
    fun delete(vararg id: Int)

    @Update
    fun update(vararg profile: Profile)

    @Query("Select * from Profile where id = :id")
    fun getById(vararg id: String): Profile?

    @Query("Delete From Profile")
    fun deleteAll()

    @Query("Select * from Profile order by scoreTicTacToe desc")
    fun getRanking(): List<Profile>

}