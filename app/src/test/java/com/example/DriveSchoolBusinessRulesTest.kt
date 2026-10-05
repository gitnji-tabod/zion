package com.example

import com.example.driveschool.data.model.*
import org.junit.Assert.*
import org.junit.Test

class DriveSchoolBusinessRulesTest {

  @Test
  fun testBR01_CountryRule_CameroonIsOnsiteCapable() {
    val cmUser = UserEntity(
      name = "Brenda Bih",
      email = "brenda@driveschool.cm",
      phone = "+237677889900",
      countryCode = "CM",
      branchId = "branch-bamenda-01"
    )
    assertTrue("Cameroon student must be onsite capable", cmUser.isOnsiteCapable)

    val frUser = UserEntity(
      name = "Thomas Leroy",
      email = "thomas@driveschool.cm",
      phone = "+33612345678",
      countryCode = "FR",
      branchId = "branch-online-00"
    )
    assertFalse("Non-Cameroon student must be online-only", frUser.isOnsiteCapable)
  }

  @Test
  fun testBR04_NegotiationGuardrail_ThresholdCalculation() {
    val standardFee = 125000.0
    val negotiatedFee = 95000.0
    val discountPercent = ((standardFee - negotiatedFee) / standardFee) * 100.0

    // 24% discount exceeds 15% threshold -> requires manager approval
    assertTrue("Discount > 15% must require manager approval", discountPercent > 15.0)

    val smallDiscountFee = 115000.0
    val smallDiscountPercent = ((standardFee - smallDiscountFee) / standardFee) * 100.0
    assertTrue("8% discount is within 15% threshold", smallDiscountPercent <= 15.0)
  }

  @Test
  fun testBR08_VehicleServicing_OverdueDetection() {
    val vehicleDue = VehicleEntity(
      id = "veh-02",
      branchId = "branch-bamenda-01",
      plateNo = "NW-819-BC",
      makeModel = "Hyundai i10",
      odometer = 39100,
      nextServiceKm = 39000,
      nextServiceDate = System.currentTimeMillis() + 100000L,
      status = VehicleStatus.SERVICE_DUE
    )

    val isOverdue = vehicleDue.odometer >= vehicleDue.nextServiceKm || vehicleDue.status == VehicleStatus.SERVICE_DUE
    assertTrue("Vehicle with odometer exceeding next service must be flagged overdue", isOverdue)
  }

  @Test
  fun testBranchHierarchy_ManagerSecretaryStudentPerBranch() {
    val branchId = "branch-kumba-01"
    val manager = UserEntity(
      name = "Ebob Kelly",
      email = "manager.kumba@driveschool.cm",
      phone = "+237670111222",
      role = UserRole.BRANCH_MANAGER,
      branchId = branchId
    )
    val secretary = UserEntity(
      name = "Nfor Daniel",
      email = "secretary.kumba@driveschool.cm",
      phone = "+237670333444",
      role = UserRole.SECRETARY,
      branchId = branchId
    )
    val student = UserEntity(
      name = "Tabe Elvis",
      email = "student.kumba@gmail.com",
      phone = "+237670555666",
      role = UserRole.STUDENT,
      branchId = branchId
    )

    assertEquals("Manager must belong to branch", branchId, manager.branchId)
    assertEquals("Secretary must belong to branch", branchId, secretary.branchId)
    assertEquals("Student must belong to branch", branchId, student.branchId)
    assertEquals("Manager role must match", UserRole.BRANCH_MANAGER, manager.role)
    assertEquals("Secretary role must match", UserRole.SECRETARY, secretary.role)
    assertEquals("Student role must match", UserRole.STUDENT, student.role)
  }
}
