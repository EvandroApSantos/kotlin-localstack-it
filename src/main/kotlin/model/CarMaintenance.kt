package model

import java.time.Instant
import java.util.UUID

data class CarMaintenance(
    val car: Car,
    val maintenanceInfo: MaintenanceInfo
)

data class Car(
    val id: UUID,
    val licensePlate: String,
    val make: String,
    val model: String
)

data class MaintenanceInfo(
    val timestamp: Instant,
    val description: String
)

data class CarMaintenanceConsolidation(
    val car: Car,
    val lastUpdate: Instant? = null,
    val maintenanceInfo: List<MaintenanceInfo>
)