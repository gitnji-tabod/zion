package com.example.driveschool.data.db

import com.example.driveschool.data.model.UserEntity
import com.example.driveschool.data.model.UserRole

object ProductionSeedData {
  val admin = UserEntity(
    id = "user-admin-01",
    name = "Platform Administrator",
    email = "admin@driveschool.cm",
    phone = "",
    countryCode = "CM",
    preferredLocale = "en",
    role = UserRole.SUPER_ADMIN,
    branchId = null
  )
}
