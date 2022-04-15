package client

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

private val credentials = StaticCredentialsProvider.create(
    AwsBasicCredentials.create("foo", "foo")
)

fun createSQSClient(address: String, region: String): SqsClient =
    with(SqsClient.builder()) {
        endpointOverride(URI.create(address))
        credentialsProvider(credentials)
        region(Region.of(region))
        build()
    }

fun createS3Client(address: String, region: String): S3Client =
    with(S3Client.builder()) {
        endpointOverride(URI.create(address))
        credentialsProvider(credentials)
        region(Region.of(region))
        build()
    }