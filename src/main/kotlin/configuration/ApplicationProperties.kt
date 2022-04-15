package configuration

import com.typesafe.config.ConfigFactory

fun loadConfiguration(): ApplicationProperties =
    with(ConfigFactory.load()) {
        val awsProperties = AWSProperties(
            address = getString("aws.address"),
            account = getString("aws.account"),
            region = getString("aws.region"),
        )
        ApplicationProperties(
            carMaintenanceQueue = SQSProperties(
                queueName = getString("aws.sqs.queueName"),
                generalProps = awsProperties
            ),
            carMaintenanceBucket = S3Properties(
                bucketName = getString("aws.s3.bucketName"),
                generalProps = awsProperties
            )
        )
    }

data class ApplicationProperties(
    val carMaintenanceQueue: SQSProperties,
    val carMaintenanceBucket: S3Properties
)

class AWSProperties(
    val address: String,
    val account: String,
    val region: String
)

data class SQSProperties(
    val queueName: String,
    val generalProps: AWSProperties
)

data class S3Properties(
    val bucketName: String,
    val generalProps: AWSProperties
)
