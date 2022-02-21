package com.abhishek.employeemanagment.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "EmployeeDB")
data class EmployeeEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int,
    @ColumnInfo(name = "Name")
    val name: String,
    @ColumnInfo(name = "Designation")
    val designation: String,
    @ColumnInfo(name = "ProfilePicURL")
    var profilePicUrl: String
) : Parcelable {
}
