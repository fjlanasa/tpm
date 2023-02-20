package tpm.processing

import tpm.api.events.{StopEvent, VehiclePosition}
import tpm.processing.EventProcessor
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import tpm.services.EventService
import tpm.api.events.VehiclePosition.StopStatus
import tpm.services.EventService
import tpm.services.EventEntityQuery

class StopEventProcessor(
    source: () => Future[Seq[VehiclePosition]],
    service: EventService
) extends EventProcessor[
      VehiclePosition,
      StopEvent,
    ](source, service)
    with VehiclePositionState(service) {
  def getEvents(
      vehiclePosition: VehiclePosition,
      state: Seq[VehiclePosition]
  ): Seq[StopEvent] = {
    state.headOption match {
      case None =>
        Seq.empty
      case Some(previousVehiclePosition)
          if previousVehiclePosition.stopId == vehiclePosition.stopId && previousVehiclePosition.stopStatus == vehiclePosition.stopStatus =>
        Seq.empty
      case Some(previousVehiclePosition) =>
        (vehiclePosition.stopStatus, previousVehiclePosition.stopStatus) match
          case (
                StopStatus.STOPPED_AT,
                StopStatus.INCOMING_AT | StopStatus.IN_TRANSIT_TO
              ) =>
            Seq(
              StopEvent(
                agencyId = vehiclePosition.agencyId,
                serviceDate = vehiclePosition.serviceDate,
                vehicleId = vehiclePosition.vehicleId,
                tripId = vehiclePosition.tripId,
                stopId = vehiclePosition.stopId,
                routeId = vehiclePosition.routeId,
                directionId = vehiclePosition.directionId,
                stopSequence = vehiclePosition.stopSequence,
                eventType = StopEvent.EventType.ARRIVAL,
                feedTimestamp = vehiclePosition.feedTimestamp
              )
            )
          case (
                StopStatus.IN_TRANSIT_TO,
                StopStatus.STOPPED_AT
              ) =>
            Seq(
              StopEvent(
                agencyId = vehiclePosition.agencyId,
                serviceDate = vehiclePosition.serviceDate,
                vehicleId = vehiclePosition.vehicleId,
                tripId = vehiclePosition.tripId,
                stopId = vehiclePosition.stopId,
                routeId = vehiclePosition.routeId,
                directionId = vehiclePosition.directionId,
                stopSequence = vehiclePosition.stopSequence,
                eventType = StopEvent.EventType.DEPARTURE,
                feedTimestamp = vehiclePosition.feedTimestamp
              )
            )
          case (StopStatus.IN_TRANSIT_TO, _) =>
            Seq(
              StopEvent(
                agencyId = previousVehiclePosition.agencyId,
                serviceDate = previousVehiclePosition.serviceDate,
                vehicleId = previousVehiclePosition.vehicleId,
                tripId = previousVehiclePosition.tripId,
                stopId = previousVehiclePosition.stopId,
                routeId = previousVehiclePosition.routeId,
                directionId = previousVehiclePosition.directionId,
                stopSequence = previousVehiclePosition.stopSequence,
                eventType = StopEvent.EventType.ARRIVAL,
                feedTimestamp = previousVehiclePosition.feedTimestamp
              ),
              StopEvent(
                agencyId = vehiclePosition.agencyId,
                serviceDate = vehiclePosition.serviceDate,
                vehicleId = vehiclePosition.vehicleId,
                tripId = vehiclePosition.tripId,
                stopId = previousVehiclePosition.stopId,
                routeId = vehiclePosition.routeId,
                directionId = vehiclePosition.directionId,
                stopSequence = vehiclePosition.stopSequence,
                eventType = StopEvent.EventType.DEPARTURE,
                feedTimestamp = vehiclePosition.feedTimestamp
              )
            )
          case _ => Seq.empty
    }
  }
}
