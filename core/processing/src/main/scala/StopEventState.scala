package tpm.processing

import tpm.api.events.StopEvent
import tpm.services.EventService
import tpm.services.EventQuery
import scala.concurrent.Future

trait StopEventState(service: EventService) {
  def getCurrentState(
      key: EventQuery[StopEvent]
  ): Future[Seq[StopEvent]] =
    service.stopEventService
      .get(
        key
      )

  def updateState(
      key: EventQuery[StopEvent],
      stopEvent: StopEvent,
      state: Seq[StopEvent]
  ): Future[Seq[StopEvent]] = {
    service.stopEventService
      .put(key, Seq(stopEvent))
  }
}
