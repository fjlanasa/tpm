package tpm.processing

import tpm.api.events.VehiclePosition
import tpm.processing.EventProcessor
import tpm.api.events.StopEvent
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import tpm.services.EventService
import tpm.services.EventEntityQuery
import tpm.services.LocalEventEntityService

class VehiclePositionProcessor(
    source: () => Future[Seq[VehiclePosition]],
    service: EventService
) extends EventProcessor[
      VehiclePosition,
      VehiclePosition,
      EventService
    ](source, service) {
  def process(
      vehiclePosition: VehiclePosition
  ): Future[Seq[VehiclePosition]] = {
    val key = getKey(vehiclePosition)
    service.vehiclePositionService
      .get(
        key
      )
      .flatMap({ case events =>
        service.vehiclePositionService
          .put(key, Seq(vehiclePosition))
          .map(_ => events)
      })
      .map(_.headOption)
      .map({
        case Some(lastVehiclePosition) =>
          if (
            lastVehiclePosition.latitude != vehiclePosition.latitude ||
            lastVehiclePosition.longitude != vehiclePosition.longitude
          ) {
            Seq(vehiclePosition)
          } else {
            Seq.empty
          }
        case None =>
          Seq(vehiclePosition)
      })
  }

  def getKey(vehiclePosition: VehiclePosition) = EventEntityQuery(
    entity = VehiclePosition(
      agencyId = vehiclePosition.agencyId,
      serviceDate = vehiclePosition.serviceDate,
      vehicleId = vehiclePosition.vehicleId
    ),
    limit = Some(1)
  )
}
