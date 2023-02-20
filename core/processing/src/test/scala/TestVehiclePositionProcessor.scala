
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.funsuite.AnyFunSuite

import tpm.processing.VehiclePositionProcessor
import scala.concurrent.Future
import tpm.api.events.VehiclePosition
import concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AsyncFlatSpec
import tpm.services.LocalEventService


class TestVehiclePositionProcessor extends AsyncFlatSpec {
  
  val source = () =>
    Future.successful(
      Seq.empty
    )
  it should "emit events if vehicle has moved" in {
    val service = new LocalEventService()
    val processor = new VehiclePositionProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          VehiclePosition(vehicleId = "1", latitude = 1, longitude = 1),
          VehiclePosition(vehicleId = "1", latitude = 1, longitude = 1),
          VehiclePosition(vehicleId = "1", latitude = 1, longitude = 2),
          VehiclePosition(vehicleId = "1", latitude = 1, longitude = 2)
        )
      )
      // one event for each unique vehicle position
      .map(events => assert(events.size == 2))
  }

  it should "not emit event if vehicle has not moved" in {
    val service = new LocalEventService()
    val processor = new VehiclePositionProcessor(
      source,
      service
    )
    processor
      .processBatch(
        List(
          VehiclePosition(vehicleId = "2", latitude = 1, longitude = 1),
          VehiclePosition(vehicleId = "2", latitude = 1, longitude = 1),
          VehiclePosition(vehicleId = "2", latitude = 1, longitude = 1)
        )
      )
      .map(events =>
        assert(
          // one event for the first vehicle position
          events.size == 1
        )
      )
  }
}
