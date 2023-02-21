import org.scalatest.funsuite.AsyncFunSuite

import tpm.processing.FeedUpdateProcessor
import tpm.services.LocalEventService

import scala.concurrent.Future
import tpm.api.events.FeedUpdate
import java.nio.file.Paths

class TestFeedUpdateProcessor extends AsyncFunSuite {
  val feedLength = 328
  val path =
      Paths.get("core/processing/src/test/resources/VehiclePositions.pb")
  val source = () =>
    Future.successful(
      Seq.empty
    )

  test("should emit events on first fetch") {
    val path =
      Paths.get("core/processing/src/test/resources/VehiclePositions.pb")
    val service = new LocalEventService()
    val processor = new FeedUpdateProcessor(
      source,
      service
    )
    processor
      .processBatch(
        Seq(FeedUpdate(agencyId = "1", feedTimestamp = 1, url = path.toUri().toString()))
      )
      .map(events => assert(events.size == feedLength))
  }

  test("should emit events if feed updates") {
    val path =
      Paths.get("core/processing/src/test/resources/VehiclePositions.pb")
    val service = new LocalEventService()
    val processor = new FeedUpdateProcessor(
      source,
      service
    )
    processor
      .processBatch(
        Seq(
          FeedUpdate(agencyId = "1", feedTimestamp = 1, url = path.toUri().toString()),
          FeedUpdate(agencyId = "1", feedTimestamp = 2, url = path.toUri().toString())
        )
      )
      .map(events => assert(events.size == feedLength * 2))
  }

  test("should not emit event if feed has not updated") {
    val path =
      Paths.get("core/processing/src/test/resources/VehiclePositions.pb")
    val service = new LocalEventService()
    val processor = new FeedUpdateProcessor(
      source,
      service
    )
    processor
      .processBatch(
        Seq(
          FeedUpdate(agencyId = "1", feedTimestamp = 1, url = path.toUri().toString()),
          FeedUpdate(agencyId = "1", feedTimestamp = 1, url = path.toUri().toString())
        )
      )
      .map(events => assert(events.size == feedLength))
  }

}
