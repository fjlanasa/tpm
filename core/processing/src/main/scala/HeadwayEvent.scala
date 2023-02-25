package tpm.processing

import tpm.api.events.StopEvent
import scala.concurrent.Future
import tpm.services.EventService
import tpm.api.events.HeadwayEvent
import tpm.services.EventQuery
import tpm.api.events.StopEvent.EventType.ARRIVAL

class HeadwayEventProcessor(
    source: () => Future[Seq[StopEvent]],
    service: EventService
) extends EventProcessor[
      StopEvent,
      HeadwayEvent,
    ](source, service)
    with StopEventState(service) {

  override def filterInputs(events: Seq[StopEvent]): Seq[StopEvent] =
    events.filter(_.eventType == ARRIVAL)

  def produceEvents(
      stopEvent: StopEvent,
      state: Seq[StopEvent]
  ): Seq[HeadwayEvent] = stopEvent.eventType match {
    case ARRIVAL =>
      state.headOption match {
        case None => Seq.empty
        case Some(previousStopEvent) =>
          Seq(
            HeadwayEvent(
              agencyId = stopEvent.agencyId,
              stopId = stopEvent.stopId,
              routeId = stopEvent.routeId,
              directionId = stopEvent.directionId,
              headwayDuration =
                stopEvent.feedTimestamp - previousStopEvent.feedTimestamp,
              tripId = stopEvent.tripId,
              latitude = stopEvent.latitude,
              longitude = stopEvent.longitude
            )
          )
      }
    case _ => Seq.empty

  }

  def getInputKey(event: StopEvent): EventQuery[StopEvent] =
    EventQuery(
      entity = StopEvent(
        agencyId = event.agencyId,
        stopId = event.stopId,
        routeId = event.routeId,
        directionId = event.directionId
      ),
      limit = Some(1)
    )
}
