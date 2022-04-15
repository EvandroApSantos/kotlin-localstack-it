import client.SQSListener
import configuration.loadConfiguration
import model.CarMaintenance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import service.CarMaintenanceConsolidationService

fun main() {
    val appProperties = loadConfiguration()
    val service = CarMaintenanceConsolidationService(appProperties.awsProperties)

    SQSListener(awsProperties = appProperties.awsProperties).start(classType = CarMaintenance::class) {
        println(it)
        it.forEach { carMaintenance -> service.consolidateMaintenance(carMaintenance) }
    }
}

val logger: Logger = LoggerFactory.getLogger("com.easantos.localstack.it.app")