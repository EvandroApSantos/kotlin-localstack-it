package configuration

import com.typesafe.config.ConfigFactory

fun loadConfiguration(): ApplicationProperties =
    with(ConfigFactory.load()) {
        ApplicationProperties(
            awsProperties = AWSProperties(
                address = getString("aws.address"),
                account = getString("aws.account"),
                region = getString("aws.region"),
                carMaintenanceQueue = SQSProperties(queueName = getString("aws.sqs.queueName")),
                carMaintenanceBucket = S3Properties(bucketName = getString("aws.s3.bucketName"))
            )
        )
    }

data class ApplicationProperties(
    val awsProperties: AWSProperties
)

data class AWSProperties(
    val address: String,
    val account: String,
    val region: String,
    val carMaintenanceQueue: SQSProperties,
    val carMaintenanceBucket: S3Properties
)

data class SQSProperties(
    val queueName: String
)

data class S3Properties(
    val bucketName: String
)