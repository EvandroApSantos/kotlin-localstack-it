package service

import client.createS3Client
import com.fasterxml.jackson.module.kotlin.readValue
import configuration.AWSProperties
import configuration.objectMapper
import logger
import model.CarMaintenance
import model.CarMaintenanceConsolidation
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.Instant
import java.util.UUID

class CarMaintenanceConsolidationService(private val awsProperties: AWSProperties) {

    private val s3Client = createS3Client(address = awsProperties.address, region = awsProperties.region)

    fun consolidateMaintenance(carMaintenance: CarMaintenance) {
        val currentConsolidation = getCurrentConsolidation(carMaintenance = carMaintenance)
        val newConsolidation = currentConsolidation.copy(
            maintenanceInfo = currentConsolidation.maintenanceInfo.plus(
                carMaintenance.maintenanceInfo
            ),
            lastUpdate = Instant.now()
        )
        persistConsolidation(newConsolidation)
        println(newConsolidation)
        logger.info("Consolidation successfully updated")
    }

    private fun getCurrentConsolidation(carMaintenance: CarMaintenance): CarMaintenanceConsolidation =
        try {
            s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(awsProperties.carMaintenanceBucket.bucketName)
                    .key(getObjectKey(carMaintenance.id))
                    .build()
            ).asByteArray().let {
                objectMapper.readValue(it)
            }
        } catch (t: NoSuchKeyException) {
            CarMaintenanceConsolidation(
                id = carMaintenance.id,
                licensePlate = carMaintenance.licensePlate,
                make = carMaintenance.make,
                model = carMaintenance.model,
                maintenanceInfo = listOf()
            )
        }

    private fun persistConsolidation(carMaintenanceConsolidation: CarMaintenanceConsolidation) =
        with(PutObjectRequest.builder()) {
            bucket(awsProperties.carMaintenanceBucket.bucketName)
                .key(getObjectKey(carMaintenanceConsolidation.id))
                .build()
        }.let {
            s3Client.putObject(it, RequestBody.fromBytes(objectMapper.writeValueAsBytes(carMaintenanceConsolidation)))
        }

    private fun getObjectKey(id: UUID): String =
        "$id.json"
}