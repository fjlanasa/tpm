package tpm.processing

import tpm.api.events.DwellEvent
import tpm.api.events.StopEvent
import tpm.services.EventService
import scala.concurrent.Future
import tpm.services.EventEntityQuery
import tpm.api.events.StopEvent.EventType

class DwellEventProcessor(
    source: () => Future[Seq[StopEvent]],
    service: EventService
) extends EventProcessor[StopEvent, DwellEvent](source, service) {
  def getInputKey(event: StopEvent): EventEntityQuery[StopEvent] =
    EventEntityQuery(
      entity = StopEvent(
        agencyId = event.agencyId,
        stopId = event.stopId,
        tripId = event.tripId,
        vehicleId = event.vehicleId
      ),
      limit = Some(1)
    )

  def getCurrentState(
      key: EventEntityQuery[StopEvent]
  ): Future[Seq[StopEvent]] = service.stopEventService.get(key)

  def processInput(input: StopEvent, state: Seq[StopEvent]): Seq[DwellEvent] =
    input.eventType match {
      case EventType.ARRIVAL => Seq.empty
      case EventType.DEPARTURE =>
        state.headOption.match {
          case Some(arrival) if arrival.eventType == EventType.ARRIVAL =>
            val dwellTime = input.feedTimestamp - arrival.feedTimestamp
            Seq(
              DwellEvent(
                agencyId = input.agencyId,
                stopId = input.stopId,
                tripId = input.tripId,
                vehicleId = input.vehicleId,
                dwellDuration = dwellTime
              )
            )
          case _ => Seq.empty
        }
      case _ => Seq.empty
    }

  def updateState(
      key: EventEntityQuery[StopEvent],
      input: StopEvent,
      state: Seq[StopEvent]
  ): Future[Seq[StopEvent]] = service.stopEventService.put(key, Seq(input))
}
