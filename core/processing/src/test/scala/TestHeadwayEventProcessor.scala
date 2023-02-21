import org.scalatest.funsuite.AsyncFunSuite

import tpm.processing.HeadwayEventProcessor
import tpm.services.LocalEventService
import tpm.api.events.StopEvent
import tpm.api.events.HeadwayEvent
import scala.concurrent.Future

class TestHeadwayEventProcessor extends AsyncFunSuite {
  val source = () =>
    Future.successful(
      Seq.empty
    )

  test("should not emit event for first stop event") {
    val service = new LocalEventService()
    val processor = new HeadwayEventProcessor(
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
            eventType = StopEvent.EventType.ARRIVAL
          )
        )
      )
      .map(events => assert(events.size == 0))
  }

  test("should emit event if stop event is an arrival") {
    val service = new LocalEventService()
    val processor = new HeadwayEventProcessor(
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
            eventType = StopEvent.EventType.ARRIVAL,
            feedTimestamp = 1
          ),
          StopEvent(
            agencyId = "2",
            stopId = "1",
            tripId = "1",
            vehicleId = "1",
            eventType = StopEvent.EventType.ARRIVAL,
            feedTimestamp = 10
          )
        )
      )
      .map(events =>
        assert(events.size == 1)
        assert(events.head.headwayDuration == 9)
      )
  }

  test("should not emit events for departures") {
    val service = new LocalEventService()
    val processor = new HeadwayEventProcessor(
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
            eventType = StopEvent.EventType.DEPARTURE,
            feedTimestamp = 1
          ),
          StopEvent(
            agencyId = "3",
            stopId = "1",
            tripId = "1",
            vehicleId = "1",
            eventType = StopEvent.EventType.DEPARTURE,
            feedTimestamp = 10
          )
        )
      )
      .map(events => assert(events.size == 0))
  }
}
