package repository

import client.createS3Client
import com.fasterxml.jackson.module.kotlin.readValue
import configuration.S3Properties
import configuration.objectMapper
import model.CarMaintenanceConsolidation
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

interface CarMaintenanceConsolidationRepository {
    fun getCurrentConsolidation(id: UUID): CarMaintenanceConsolidation?
    fun persistConsolidation(carMaintenanceConsolidation: CarMaintenanceConsolidation)
}

class AWSCarMaintenanceConsolidationRepository(
    private val s3Properties: S3Properties
) : CarMaintenanceConsolidationRepository {
    private val s3Client =
        createS3Client(address = s3Properties.address, region = s3Properties.region)

    override fun getCurrentConsolidation(id: UUID): CarMaintenanceConsolidation? =
        try {
            s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(s3Properties.bucketName)
                    .key(getObjectKey(id))
                    .build()
            ).asByteArray().let {
                objectMapper.readValue(it)
            }
        } catch (t: NoSuchKeyException) {
            null
        }

    override fun persistConsolidation(carMaintenanceConsolidation: CarMaintenanceConsolidation) {
        with(PutObjectRequest.builder()) {
            bucket(s3Properties.bucketName)
            key(getObjectKey(carMaintenanceConsolidation.car.id))
            build()
        }.let {
            s3Client.putObject(it, RequestBody.fromBytes(objectMapper.writeValueAsBytes(carMaintenanceConsolidation)))
        }
    }

    private fun getObjectKey(id: UUID): String =
        "$id.json"
}