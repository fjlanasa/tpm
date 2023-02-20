package tpm.processing

import tpm.api.events.VehiclePosition
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import tpm.services.EventService
import tpm.services.EventEntityQuery

trait EventProcessor[I, O](
    source: () => Future[Seq[I]],
    service: EventService
) {
  def process(input: I): Future[Seq[O]] = {
    val key = getInputKey(input)
    getCurrentState(key)
      .map(produceEvents(input, _))
      .flatMap({ case outputs =>
        onComplete(key, input)
          .map(_ => outputs)
      })
  }

  def processBatch(events: Seq[I]): Future[Seq[O]] = Future {
    events
      .map(event => {
        Await.result(
          process(event),
          concurrent.duration.Duration(10, "seconds")
        )
      })
      .flatten
  }

  def getBatch(): Future[Seq[I]] = source()

  def getInputKey(event: I): EventEntityQuery[I]

  def getCurrentState(key: EventEntityQuery[I]): Future[Seq[I]]

  def produceEvents(input: I, state: Seq[I]): Seq[O]

  def onComplete(
      key: EventEntityQuery[I],
      input: I,
  ): Future[_]
}
