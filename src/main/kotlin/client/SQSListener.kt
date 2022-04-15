package client

import configuration.AWSProperties
import configuration.objectMapper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import logger
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.OverLimitException
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import kotlin.reflect.KClass

class SQSListener(private val awsProperties: AWSProperties) {
    fun <T : Any> start(classType: KClass<T>, messageProcessor: (List<T>) -> Unit) {
        runBlocking {
            launch {
                val sqsClient = createSQSClient(address = awsProperties.address, region = awsProperties.region)
                val messageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(getQueueUrl(awsProperties))
                    .waitTimeSeconds(20)
                    .maxNumberOfMessages(1)
                    .build()

                logger.info("SQS Listener started")

                while (true) {
                    try {
                        sqsClient.receiveMessage(messageRequest).messages().let {
                            messageProcessor(it.map { message ->
                                objectMapper.readValue(message.body(), classType.java)
                            })
                            deleteMessages(sqsClient, it)
                        }
                    } catch (t: OverLimitException) {
                        logger.warn("OverLimitException has been thrown")
                    } catch (t: Throwable) {
                        logger.error("Error during message processing", t)
                    }
                }
            }
        }
    }

    private fun deleteMessages(sqsClient: SqsClient, messages: List<Message>) =
        messages.forEach {
            sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                    .queueUrl(getQueueUrl(awsProperties))
                    .receiptHandle(it.receiptHandle())
                    .build()
            )
        }

    private fun getQueueUrl(awsProperties: AWSProperties): String =
        "${awsProperties.address}/${awsProperties.account}/${awsProperties.carMaintenanceQueue.queueName}"
}