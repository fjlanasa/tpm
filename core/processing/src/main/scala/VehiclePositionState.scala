package tpm.processing

import tpm.api.events.VehiclePosition
import tpm.processing.EventProcessor
import tpm.api.events.StopEvent
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import tpm.services.EventService
import tpm.services.EventEntityQuery


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

  def updateState(
      key: EventEntityQuery[VehiclePosition],
      vehiclePosition: VehiclePosition,
      state: Seq[VehiclePosition]
  ): Future[Seq[VehiclePosition]] = {
    service.vehiclePositionService
      .put(key, Seq(vehiclePosition))
  }
}