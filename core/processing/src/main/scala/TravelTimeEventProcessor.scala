package tpm.processing

import tpm.processing.EventProcessor
import tpm.api.events.StopEvent
import scala.concurrent.Future
import tpm.processing.StopEventState
import tpm.services.EventService
import tpm.api.events.TravelTimeEvent
import tpm.services.EventQuery
import tpm.api.events.StopEvent.EventType
import concurrent.ExecutionContext.Implicits.global

class TravelTimeEventProcessor(
    source: () => Future[Seq[StopEvent]],
    service: EventService
) extends EventProcessor[StopEvent, TravelTimeEvent](source, service)
    with StopEventState(service) {
  def getInputKey(event: StopEvent): EventQuery[StopEvent] =
    EventQuery(
      entity = StopEvent(
        agencyId = event.agencyId,
        tripId = event.tripId
      ),
      limit = Some(1)
    )

  def produceEvents(
      input: StopEvent,
      state: Seq[StopEvent]
  ): Seq[TravelTimeEvent] = input.eventType match {
    case EventType.DEPARTURE => Seq.empty
    case EventType.ARRIVAL =>
      state.map(arrival => {
        val travelTime = input.feedTimestamp - arrival.feedTimestamp
        TravelTimeEvent(
          agencyId = input.agencyId,
          tripId = input.tripId,
          routeId = input.routeId,
          directionId = input.directionId,
          stopId = input.stopId,
          originStopId = arrival.stopId,
          travelDuration = travelTime
        )
      })

    case _ => Seq.empty
  }

  override def updateState(
      key: EventQuery[StopEvent],
      input: StopEvent,
      state: Seq[StopEvent]
  ): Future[Seq[StopEvent]] = {
    val newState = input.eventType match {
      case EventType.ARRIVAL   => state :+ input
      case EventType.DEPARTURE => state
      case _                   => state
    }
    service.stopEventService.put(key, newState)
  }
}
