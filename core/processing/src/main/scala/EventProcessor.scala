package tpm.processing

import tpm.api.events.VehiclePosition
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import tpm.services.EventService
import tpm.services.EventQuery

trait EventProcessor[I, O](
    val source: () => Future[Seq[I]],
    val service: EventService
) {
  def processEvent(input: I): Future[Seq[O]] = {
    // get the key for the input
    // get the current state for the key
    // get the events for the input and state
    // update the state for the key
    // return the events
    val key = getInputKey(input)
    getCurrentState(key)
      .flatMap(state => {
        updateState(key, input, state).map(_ => produceEvents(input, state))
      })
  }

  def processBatch(events: Seq[I]): Future[Seq[O]] = Future {
    filterInputs(events)
      .map(event => {
        Await.result(
          processEvent(event),
          concurrent.duration.Duration(10, "seconds")
        )
      })
      .flatten
  }

  def filterInputs(events: Seq[I]): Seq[I] = events

  def getBatch(): Future[Seq[I]] = source()

  def getInputKey(event: I): EventQuery[I]

  def getCurrentState(key: EventQuery[I]): Future[Seq[I]]

  def produceEvents(input: I, state: Seq[I]): Seq[O]

  def updateState(
      key: EventQuery[I],
      input: I,
      state: Seq[I]
  ): Future[Seq[I]]
}
