package tpm.services

import scala.concurrent.Future

import concurrent.ExecutionContext.Implicits.global
import tpm.api.events.VehiclePosition
import tpm.api.events.StopEvent
import tpm.api.events.FeedUpdate

class LocalEventEntityService[T, K](
    private var state: Map[K, Seq[T]] = Map[K, Seq[T]]()
) extends EventEntityService[T, K] {
  def get(key: K): Future[Seq[T]] = Future {
    state.getOrElse(key, Seq.empty)
  }

  def put(key: K, value: Seq[T]): Future[Seq[T]] = Future {
    state = state + (key -> value)
    value
  }

  def delete(key: K) = Future {
    val value = state.getOrElse(key, Seq.empty)
    state = state - key
    value
  }
}

class LocalEventService
    extends EventService(
      new LocalEventEntityService[VehiclePosition, EventQuery[
        VehiclePosition
      ]],
      new LocalEventEntityService[StopEvent, EventQuery[StopEvent]],
      new LocalEventEntityService[FeedUpdate, EventQuery[FeedUpdate]]
    )
