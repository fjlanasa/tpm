package tpm.processing

import tpm.api.events.VehiclePosition
import tpm.processing.EventProcessor
import tpm.api.events.StopEvent
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import tpm.services.EventService
import tpm.services.EventEntityQuery
import tpm.services.LocalEventEntityService

trait VehiclePositionState(service: EventService) {
  def getInputKey(
      vehiclePosition: VehiclePosition
  ) = EventEntityQuery(
    entity = VehiclePosition(
      agencyId = vehiclePosition.agencyId,
      serviceDate = vehiclePosition.serviceDate,
      vehicleId = vehiclePosition.vehicleId
    ),
    limit = Some(1)
  )

  def getCurrentState(
      key: EventEntityQuery[VehiclePosition]
  ): Future[Seq[VehiclePosition]] =
    service.vehiclePositionService
      .get(
        key
      )

  def onComplete(
      key: EventEntityQuery[VehiclePosition],
      vehiclePosition: VehiclePosition,
      state: Seq[VehiclePosition]
  ): Future[Seq[VehiclePosition]] = {
    service.vehiclePositionService
      .put(key, Seq(vehiclePosition))
  }
}

class VehiclePositionProcessor(
    source: () => Future[Seq[VehiclePosition]],
    service: EventService
) extends EventProcessor[
      VehiclePosition,
      VehiclePosition,
    ](source, service)
    with VehiclePositionState(service) {
  def produceEvents(
      vehiclePosition: VehiclePosition,
      state: Seq[VehiclePosition]
  ): Seq[VehiclePosition] = {
    state.headOption match {
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
    }
  }
}
