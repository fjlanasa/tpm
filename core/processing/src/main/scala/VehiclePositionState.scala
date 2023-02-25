package tpm.processing

import tpm.api.events.VehiclePosition
import tpm.processing.EventProcessor
import tpm.api.events.StopEvent
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import tpm.services.EventService
import tpm.services.EventQuery

trait VehiclePositionState(service: EventService) {
  def getInputKey(
      vehiclePosition: VehiclePosition
  ) = EventQuery(
    entity = VehiclePosition(
      agencyId = vehiclePosition.agencyId,
      serviceDate = vehiclePosition.serviceDate,
      vehicleId = vehiclePosition.vehicleId
    ),
    limit = Some(1)
  )

  def getCurrentState(
      key: EventQuery[VehiclePosition]
  ): Future[Seq[VehiclePosition]] =
    service.vehiclePositionService
      .get(
        key
      )

  def updateState(
      key: EventQuery[VehiclePosition],
      vehiclePosition: VehiclePosition,
      state: Seq[VehiclePosition]
  ): Future[Seq[VehiclePosition]] = {
    service.vehiclePositionService
      .put(key, Seq(vehiclePosition))
  }
}
