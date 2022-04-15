package service

import configuration.AWSProperties
import logger
import model.CarMaintenance
import model.CarMaintenanceConsolidation
import repository.CarMaintenanceConsolidationRepository
import java.time.Instant

class CarMaintenanceConsolidationService(awsProperties: AWSProperties) {
    private val repository = CarMaintenanceConsolidationRepository(awsProperties)

    fun consolidateMaintenance(carMaintenance: CarMaintenance) {
        val currentConsolidation = getCurrentConsolidation(carMaintenance = carMaintenance)
        val newConsolidation = currentConsolidation.copy(
            maintenanceInfo = currentConsolidation.maintenanceInfo.plus(
                carMaintenance.maintenanceInfo
            ),
            lastUpdate = Instant.now()
        )
        persistConsolidation(newConsolidation)
        logger.info("Consolidation successfully updated for id {}", newConsolidation.id)
    }

    private fun getCurrentConsolidation(carMaintenance: CarMaintenance): CarMaintenanceConsolidation =
        repository.getCurrentConsolidation(carMaintenance.id)
            ?: CarMaintenanceConsolidation(
                id = carMaintenance.id,
                licensePlate = carMaintenance.licensePlate,
                make = carMaintenance.make,
                model = carMaintenance.model,
                maintenanceInfo = listOf()
            )

    private fun persistConsolidation(carMaintenanceConsolidation: CarMaintenanceConsolidation) =
        repository.persistConsolidation(carMaintenanceConsolidation)
}