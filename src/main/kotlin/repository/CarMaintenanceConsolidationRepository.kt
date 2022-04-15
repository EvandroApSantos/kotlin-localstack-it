package repository

import client.createS3Client
import com.fasterxml.jackson.module.kotlin.readValue
import configuration.AWSProperties
import configuration.objectMapper
import model.CarMaintenanceConsolidation
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

class CarMaintenanceConsolidationRepository(
    private val awsProperties: AWSProperties
) {
    private val s3Client = createS3Client(address = awsProperties.address, region = awsProperties.region)

    fun getCurrentConsolidation(id: UUID): CarMaintenanceConsolidation? =
        try {
            s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(awsProperties.carMaintenanceBucket.bucketName)
                    .key(getObjectKey(id))
                    .build()
            ).asByteArray().let {
                objectMapper.readValue(it)
            }
        } catch (t: NoSuchKeyException) {
            null
        }

    fun persistConsolidation(carMaintenanceConsolidation: CarMaintenanceConsolidation) =
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