package tpm.processing

import tpm.api.events.VehiclePosition
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import tpm.services.EventService

trait EventProcessor[I, O](
    source: () => Future[Seq[I]],
    service: EventService
) {
  def process(event: I): Future[Seq[O]]

  def processBatch(events: Seq[I]): Future[Seq[O]] = Future {
    events
      .map(event => {
        val events = Await.result(
          process(event),
          concurrent.duration.Duration(10, "seconds")
        )
        events
      })
      .flatten
  }

  def getBatch(): Future[Seq[I]] = source()
}
