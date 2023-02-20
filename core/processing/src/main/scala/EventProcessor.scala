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
  def processEvent(input: I): Future[Seq[O]] = {
    // get the key for the input
    // get the current state for the key
    // get the events for the input and state
    // update the state for the key
    // return the events
    val key = getInputKey(input)
    getCurrentState(key)
      .flatMap(state => {
        val events = getEvents(input, state)
        updateState(key, input, state).map(_ => events)
      })
  }

  def processBatch(events: Seq[I]): Future[Seq[O]] = Future {
    filterEvents(events)
      .map(event => {
        Await.result(
          processEvent(event),
          concurrent.duration.Duration(10, "seconds")
        )
      })
      .flatten
  }

  def filterEvents(events: Seq[I]): Seq[I] = events

  def getBatch(): Future[Seq[I]] = source()

  def getInputKey(event: I): EventEntityQuery[I]

  def getCurrentState(key: EventEntityQuery[I]): Future[Seq[I]]

  def getEvents(input: I, state: Seq[I]): Seq[O]

  def updateState(
      key: EventEntityQuery[I],
      input: I,
      state: Seq[I]
  ): Future[_]
}
