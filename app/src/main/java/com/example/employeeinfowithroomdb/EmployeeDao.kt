package com.example.employeeinfowithroomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert
    suspend fun insert(employeeEntity: EmployeeEntity)

    @Update
    suspend fun update(employeeEntity: EmployeeEntity)

    @Delete
    suspend fun delete(employeeEntity: EmployeeEntity)

    @Query(value = "SELECT * FROM `employee`")
    fun fetchAllEmployees(): Flow<List<EmployeeEntity>>

    @Query(value = "SELECT * FROM `employee` WHERE id = :id")
    fun fetchEmployeeById(id: Int): Flow<EmployeeEntity>
}