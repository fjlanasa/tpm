import org.scalatest.funsuite.AsyncFunSuite
import tpm.processing.TravelTimeEventProcessor
import tpm.services.LocalEventService
import tpm.api.events.StopEvent
import tpm.api.events.TravelTimeEvent
import scala.concurrent.Future

class TestTravelTimeEventProcessor extends AsyncFunSuite {
  val source = () =>
    Future.successful(
      Seq.empty
    )
  test("should not emit event for first stop event") {
    val service = new LocalEventService()
    val processor = new TravelTimeEventProcessor(
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

  test("should emit events for subsequent arrivals") {
    val service = new LocalEventService()
    val processor = new TravelTimeEventProcessor(
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
            stopId = "2",
            tripId = "1",
            vehicleId = "1",
            eventType = StopEvent.EventType.ARRIVAL,
            feedTimestamp = 10
          ),
          StopEvent(
            agencyId = "2",
            stopId = "3",
            tripId = "1",
            vehicleId = "1",
            eventType = StopEvent.EventType.ARRIVAL,
            feedTimestamp = 20
          )
        )
      )
      .map(events =>
        assert(
          events == List(
            TravelTimeEvent(
              agencyId = "2",
              tripId="1",
              stopId="2",
              originStopId="1",
              travelDuration = 9,
            ),
            TravelTimeEvent(
              agencyId = "2",
              tripId = "1",
              stopId="3",
              originStopId="1",
              travelDuration=19,
            ),
            TravelTimeEvent(
              agencyId = "2",
              tripId="1",
              stopId="3",
              originStopId="2",
              travelDuration=10,
            )
          )
        )
        assert(events.size == 3)
      )
  }

}
