
package tpm.services

import org.scalatest.funsuite.AsyncFunSuite

import tpm.processing.StopEventProcessor

import scala.concurrent.Future
import tpm.api.events.VehiclePosition
import tpm.api.events.VehiclePosition.StopStatus
import tpm.api.events.StopEvent.EventType
import tpm.services.LocalEventService

class TestStopEventProcessor extends AsyncFunSuite {
  val source = () =>
    Future.successful(
      Seq.empty
    )
  
  test("should not emit event for first vehicle position") {
    val service = new LocalEventService()
    val processor = new StopEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          VehiclePosition(
            vehicleId = "1",
            stopId = "1",
            stopStatus = StopStatus.IN_TRANSIT_TO
          )
        )
      )
      .map(events => assert(events.size == 0))
  }

  test("should not emit event if vehicle has not moved") {
    val service = new LocalEventService()
    val processor = new StopEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          VehiclePosition(
            vehicleId = "2",
            stopId = "1",
            stopStatus = StopStatus.STOPPED_AT
          ),
          VehiclePosition(
            vehicleId = "2",
            stopId = "1",
            stopStatus = StopStatus.STOPPED_AT
          )
        )
      )
      .map(events => assert(events.size == 0))
  }

  test("should emit arrivals") {
    val service = new LocalEventService()
    val processor = new StopEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          VehiclePosition(
            vehicleId = "3",
            stopId = "1",
            stopStatus = StopStatus.IN_TRANSIT_TO
          ),
          VehiclePosition(
            vehicleId = "3",
            stopId = "1",
            stopStatus = StopStatus.STOPPED_AT
          )
        )
      )
      .map(events => {
        assert(events.size == 1)
        assert(events.head.stopId ==  "1")
        assert(events.head.vehicleId == "3")
        assert(events.head.eventType == EventType.ARRIVAL)
      })
  }

  test("should emit departures") {
    val service = new LocalEventService()
    val processor = new StopEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          VehiclePosition(
            vehicleId = "4",
            stopId = "1",
            stopStatus = StopStatus.STOPPED_AT
          ),
          VehiclePosition(
            vehicleId = "4",
            stopId = "1",
            stopStatus = StopStatus.IN_TRANSIT_TO
          )
        )
      )
      .map(events => {
        assert(events.size == 1)
        assert(events.head.stopId == "1")
        assert(events.head.vehicleId == "4")
        assert(events.head.eventType == EventType.DEPARTURE)
      })
  }

  test("should emit arrival and departure if STOPPED_AT is not observed") {
    val service = new LocalEventService()
    val processor = new StopEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          VehiclePosition(
            vehicleId = "5",
            stopId = "1",
            stopStatus = StopStatus.IN_TRANSIT_TO,
            feedTimestamp = 1
          ),
          VehiclePosition(
            vehicleId = "5",
            stopId = "2",
            stopStatus = StopStatus.IN_TRANSIT_TO,
            feedTimestamp = 2
          )
        )
      )
      .map(events => {
        val firstEvent = events(0)
        val secondEvent = events(1)
        assert(events.size == 2)
        assert(firstEvent.stopId ==  "1")
        assert(firstEvent.vehicleId == "5")
        assert(firstEvent.eventType == EventType.ARRIVAL)
        assert(firstEvent.feedTimestamp == 1)
        assert(secondEvent.stopId == "1")
        assert(secondEvent.vehicleId == "5")
        assert(secondEvent.eventType == EventType.DEPARTURE)
        assert(secondEvent.feedTimestamp == 2)
      })
  }
}
