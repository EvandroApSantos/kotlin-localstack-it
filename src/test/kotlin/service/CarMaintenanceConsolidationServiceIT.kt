package service

import appProperties
import client.createS3Client
import client.createSQSClient
import com.fasterxml.jackson.module.kotlin.readValue
import configuration.objectMapper
import fixtures.car1
import fixtures.car2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.Car
import model.CarMaintenance
import model.CarMaintenanceConsolidation
import model.MaintenanceInfo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import startup
import java.time.Instant
import java.util.UUID

@TestInstance(Lifecycle.PER_CLASS)
class CarMaintenanceConsolidationServiceIT {
    private lateinit var sqsClient: SqsClient
    private lateinit var s3Client: S3Client

    @BeforeAll
    fun beforeAll() {
        CoroutineScope(Dispatchers.Default).launch { startup() }

        with(appProperties.carMaintenanceQueue) {
            sqsClient = createSQSClient(
                address = address,
                region = region
            )
        }

        with(appProperties.carMaintenanceBucket) {
            s3Client = createS3Client(
                address = address,
                region = region
            )
        }
    }

    @Test
    fun `check consolidation workflow`() {
        publishMessageOnQueue(car1.copy(), "Fuel filter replacement")
        publishMessageOnQueue(car2.copy(), "Carburetor cleanup")
        publishMessageOnQueue(car1.copy(), "Engine oil change")

        waitForDequeueMessages()

        val car1Maintenance: CarMaintenanceConsolidation? = getMaintenanceConsolidation(car1.id)
        Assertions.assertNotNull(car1Maintenance, "Consolidation for car1 has been found")
        Assertions.assertEquals(car1Maintenance?.maintenanceInfo?.size, 2, "There are 2 maintenances for car1")
        Assertions.assertArrayEquals(
            car1Maintenance?.maintenanceInfo?.map { it.description }?.toTypedArray(),
            arrayOf("Fuel filter replacement", "Engine oil change"),
            "Maintenance descriptions checked for car1"
        )

        val car2Maintenance: CarMaintenanceConsolidation? = getMaintenanceConsolidation(car2.id)
        Assertions.assertNotNull(car2Maintenance, "Consolidation for car2 has been found")
        Assertions.assertEquals(car2Maintenance?.maintenanceInfo?.size, 1, "There is 1 maintenance for car2")
        Assertions.assertArrayEquals(
            car2Maintenance?.maintenanceInfo?.map { it.description }?.toTypedArray(),
            arrayOf("Carburetor cleanup"),
            "Maintenance description checked for car2"
        )
    }

    private fun waitForDequeueMessages(timeoutInMills: Long = 10_000) {
        val sqsAttrRequests = with(GetQueueAttributesRequest.builder()) {
            queueUrl(appProperties.carMaintenanceQueue.getQueueUrl())
            attributeNames(
                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE
            )
            build()
        }

        val start = Instant.now()
        while (start.plusMillis(timeoutInMills) >= Instant.now()) {
            runBlocking { delay(200) }

            val isEmpty = sqsClient
                .getQueueAttributes(sqsAttrRequests)
                .attributes().all {
                    it.value.toInt() == 0
                }
            if (isEmpty)
                return
        }
        throw RuntimeException("Timeout waiting for dequeue messages")
    }

    private fun publishMessageOnQueue(car: Car, maintenanceDescription: String) =
        with(SendMessageRequest.builder()) {
            queueUrl(appProperties.carMaintenanceQueue.getQueueUrl())
            messageBody(
                objectMapper.writeValueAsString(
                    CarMaintenance(
                        car = car,
                        maintenanceInfo = MaintenanceInfo(
                            timestamp = Instant.now(),
                            description = maintenanceDescription
                        )
                    )
                )
            )
            sqsClient.sendMessage(build())
        }

    private fun getMaintenanceConsolidation(carId: UUID): CarMaintenanceConsolidation? =
        with(GetObjectRequest.builder()) {
            bucket(appProperties.carMaintenanceBucket.bucketName)
            key("$carId.json")
            objectMapper.readValue(s3Client.getObject(build()).readAllBytes())
        }
}
