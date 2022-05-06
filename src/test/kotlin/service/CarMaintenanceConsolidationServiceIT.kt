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
import startup

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
        //check s3
    }
}