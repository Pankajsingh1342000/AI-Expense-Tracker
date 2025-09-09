package com.example.aiexpensetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey
    val name: String,
    val keywords: String,
    val isDefault: Boolean = false
)
