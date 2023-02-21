package tpm.processing

import tpm.api.events.VehiclePosition
import tpm.processing.EventProcessor
import tpm.api.events.StopEvent
import scala.concurrent.Future
import tpm.services.EventService
import tpm.services.EventEntityQuery
import tpm.services.LocalEventEntityService

class VehiclePositionProcessor(
    source: () => Future[Seq[VehiclePosition]],
    service: EventService
) extends EventProcessor[
      VehiclePosition,
      VehiclePosition,
    ](source, service)
    with VehiclePositionState(service) {
  def processInput(
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
