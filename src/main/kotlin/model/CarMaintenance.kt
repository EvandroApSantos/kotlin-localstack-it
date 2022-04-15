package model

import java.time.Instant
import java.util.UUID

data class CarMaintenance(
    val id: UUID,
    val licensePlate: String,
    val make: String,
    val model: String,
    val maintenanceInfo: MaintenanceInfo
)

data class MaintenanceInfo(
    val timestamp: Instant,
    val description: String
)

data class CarMaintenanceConsolidation(
    val id: UUID,
    val licensePlate: String,
    val make: String,
    val model: String,
    val lastUpdate: Instant? = null,
    val maintenanceInfo: List<MaintenanceInfo>
)