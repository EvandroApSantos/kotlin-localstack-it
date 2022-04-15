package configuration

import com.typesafe.config.ConfigFactory

fun loadConfiguration(): ApplicationProperties =
    with(ConfigFactory.load()) {
        ApplicationProperties(
            carMaintenanceQueue = SQSProperties(
                queueName = getString("aws.sqs.queueName"),
                address = getString("aws.address"),
                account = getString("aws.account"),
                region = getString("aws.region"),
            ),
            carMaintenanceBucket = S3Properties(
                bucketName = getString("aws.s3.bucketName"),
                address = getString("aws.address"),
                region = getString("aws.region"),
            )
        )
    }

data class ApplicationProperties(
    val carMaintenanceQueue: SQSProperties,
    val carMaintenanceBucket: S3Properties
)

data class SQSProperties(
    val queueName: String,
    val address: String,
    val account: String,
    val region: String
)

data class S3Properties(
    val bucketName: String,
    val address: String,
    val region: String
)
