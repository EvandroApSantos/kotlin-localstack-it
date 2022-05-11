package fixtures

import model.Car
import java.util.UUID

val car1 = Car(
    id = UUID.randomUUID(),
    licensePlate = "AB1C23D",
    make = "Chevrolet",
    model = "Vectra"
)

val car2 = Car(
    id = UUID.randomUUID(),
    licensePlate = "12A3BC4",
    make = "Chevrolet",
    model = "Opala"
)
