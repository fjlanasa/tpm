import org.scalatest.funsuite.AsyncFunSuite

import tpm.processing.DwellEventProcessor

import scala.concurrent.Future
import tpm.api.events.StopEvent
import tpm.api.events.DwellEvent
import tpm.api.events.StopEvent.EventType
import tpm.services.LocalEventService

class TestDwellEventProcessor extends AsyncFunSuite {
  val source = () =>
    Future.successful(
      Seq.empty
    )

  test("should not emit event for first stop event") {
    val service = new LocalEventService()
    val processor = new DwellEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          StopEvent(
            agencyId = "1",
            stopId = "1",
            tripId = "1",
            vehicleId = "1",
            eventType = EventType.ARRIVAL
          )
        )
      )
      .map(events => assert(events.size == 0))
  }

  test("should not emit event if stop event is not a departure") {
    val service = new LocalEventService()
    val processor = new DwellEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          StopEvent(
            agencyId = "2",
            stopId = "1",
            tripId = "1",
            vehicleId = "1",
            eventType = EventType.ARRIVAL
          ),
          StopEvent(
            agencyId = "2",
            stopId = "1",
            tripId = "1",
            vehicleId = "1",
            eventType = EventType.ARRIVAL
          )
        )
      )
      .map(events => assert(events.size == 0))
  }

  test("should emit event if stop event is a departure") {
    val service = new LocalEventService()
    val processor = new DwellEventProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          StopEvent(
            agencyId = "3",
            stopId = "1",
            tripId = "1",
            vehicleId = "1",
            feedTimestamp = 1,
            eventType = EventType.ARRIVAL
          ),
          StopEvent(
            agencyId = "3",
            stopId = "1",
            tripId = "1",
            vehicleId = "1",
            feedTimestamp = 10,
            eventType = EventType.DEPARTURE
          )
        )
      )
      .map(events => 
        assert(events.size == 1)
        assert(events.head.isInstanceOf[DwellEvent])
        assert(events.head.stopId == "1")
        assert(events.head.dwellDuration == 9)
    )
  }

}
