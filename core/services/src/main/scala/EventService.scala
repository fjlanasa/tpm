package tpm.services

import scala.concurrent.Future
import tpm.api.events.VehiclePosition
import tpm.api.events.StopEvent
import tpm.api.events.FeedUpdate

trait EventEntityService[T, K] {
  def get(key: K): Future[Seq[T]]

  def put(key: K, value: Seq[T]): Future[Seq[T]]

  def delete(key: K): Future[Seq[T]]
}

class EventService(
    val vehiclePositionService: EventEntityService[
      VehiclePosition,
      EventQuery[VehiclePosition]
    ],
    val stopEventService: EventEntityService[StopEvent, EventQuery[
      StopEvent
    ]],
    val feedUpdateService: EventEntityService[FeedUpdate, EventQuery[
      FeedUpdate
    ]]
)
