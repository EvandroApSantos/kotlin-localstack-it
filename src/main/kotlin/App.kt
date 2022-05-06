import client.SQSListener
import configuration.ApplicationProperties
import configuration.loadConfiguration
import model.CarMaintenance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repository.AWSCarMaintenanceConsolidationRepository
import service.CarMaintenanceConsolidationService

fun main() {
    startup()
}

fun startup() {
    val repository = AWSCarMaintenanceConsolidationRepository(appProperties.carMaintenanceBucket)
    val service = CarMaintenanceConsolidationService(repository = repository)

    SQSListener(sqsProperties = appProperties.carMaintenanceQueue).start(classType = CarMaintenance::class) {
        logger.debug("Message received: {}", it)
        it.forEach { carMaintenance -> service.consolidateMaintenance(carMaintenance) }
    }
}

val appProperties: ApplicationProperties = loadConfiguration()
val logger: Logger = LoggerFactory.getLogger("com.easantos.localstack.it.app")