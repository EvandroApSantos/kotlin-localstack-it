package service

import logger
import model.Car
import model.CarMaintenance
import model.CarMaintenanceConsolidation
import repository.CarMaintenanceConsolidationRepository
import java.time.Instant

class CarMaintenanceConsolidationService(private val repository: CarMaintenanceConsolidationRepository) {

    fun consolidateMaintenance(carMaintenance: CarMaintenance) {
        val currentConsolidation = getCurrentConsolidation(carMaintenance = carMaintenance)
        val newConsolidation = currentConsolidation.copy(
            maintenanceInfo = currentConsolidation.maintenanceInfo.plus(
                carMaintenance.maintenanceInfo
            ),
            lastUpdate = Instant.now()
        )
        persistConsolidation(newConsolidation)
        logger.info("Consolidation successfully updated for id {}", newConsolidation.car.id)
    }

    private fun getCurrentConsolidation(carMaintenance: CarMaintenance): CarMaintenanceConsolidation =
        repository.getCurrentConsolidation(carMaintenance.car.id)
            ?: CarMaintenanceConsolidation(
                car = Car(
                    id = carMaintenance.car.id,
                    licensePlate = carMaintenance.car.licensePlate,
                    make = carMaintenance.car.make,
                    model = carMaintenance.car.model,
                ),
                maintenanceInfo = listOf()
            )

    private fun persistConsolidation(carMaintenanceConsolidation: CarMaintenanceConsolidation) =
        repository.persistConsolidation(carMaintenanceConsolidation)
}