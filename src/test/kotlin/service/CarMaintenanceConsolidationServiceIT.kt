package service

import appProperties
import client.createS3Client
import client.createSQSClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import startup
import java.time.Instant

@TestInstance(Lifecycle.PER_CLASS)
class CarMaintenanceConsolidationServiceIT {
    private lateinit var sqsClient: SqsClient
    private lateinit var s3Client: S3Client

    @BeforeAll
    fun beforeAll() {
        startup()

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
        //publish on sqs
        //publish on sqs
        //publish on sqs
        //wait for queue
        waitForDequeueMessages(10_000)
        //check s3
    }

    private fun waitForDequeueMessages(timeoutInMills: Long) {
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
}