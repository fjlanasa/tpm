package tpm.processing

import tpm.api.events.FeedUpdate
import tpm.api.events.VehiclePosition

import tpm.processing.EventProcessor
import java.net.URL
import tpm.services.EventEntityQuery
import scala.concurrent.Future
import tpm.services.EventService
import scala.jdk.CollectionConverters._

import com.google.transit.realtime.GtfsRealtime.{
  FeedEntity,
  FeedMessage,
  VehiclePosition => GtfsVehiclePosition
}

class FeedUpdateProcessor(
    source: () => Future[Seq[FeedUpdate]],
    service: EventService
) extends EventProcessor[
      FeedUpdate,
      VehiclePosition,
    ](source, service) {
  def produceEvents(
      feedUpdate: FeedUpdate,
      state: Seq[FeedUpdate]
  ): Seq[VehiclePosition] = state.headOption match {
    case Some(lastUpdate)
        if lastUpdate.feedTimestamp == feedUpdate.feedTimestamp =>
      Seq.empty
    case _ =>
      val url = new URL(feedUpdate.url)
      val message = FeedMessage.parseFrom(url.openStream())
      message
        .getEntityList()
        .asScala
        .toSeq
        .map(x =>
          VehiclePosition(
            agencyId = feedUpdate.agencyId,
            vehicleId = x.getVehicle().getVehicle().getId(),
            routeId = x.getVehicle().getTrip().getRouteId(),
            tripId = x.getVehicle().getTrip().getTripId(),
            directionId = x.getVehicle().getTrip().getDirectionId(),
            stopId = x.getVehicle().getStopId(),
            stopStatus = x.getVehicle().getCurrentStatus() match {
              case GtfsVehiclePosition.VehicleStopStatus.IN_TRANSIT_TO =>
                VehiclePosition.StopStatus.IN_TRANSIT_TO
              case GtfsVehiclePosition.VehicleStopStatus.STOPPED_AT =>
                VehiclePosition.StopStatus.STOPPED_AT
              case GtfsVehiclePosition.VehicleStopStatus.INCOMING_AT =>
                VehiclePosition.StopStatus.INCOMING_AT
            },
            feedTimestamp = x.getVehicle().getTimestamp(),
            latitude = x.getVehicle().getPosition().getLatitude(),
            longitude = x.getVehicle().getPosition().getLongitude()
          )
        )
  }

  def getInputKey(event: FeedUpdate): EventEntityQuery[FeedUpdate] = {
    EventEntityQuery(
      entity = FeedUpdate(
        agencyId = event.agencyId
      ),
      limit = Some(1)
    )
  }

  def updateState(
      key: EventEntityQuery[FeedUpdate],
      input: FeedUpdate,
      state: Seq[FeedUpdate]
  ) = {
    service.feedUpdateService.put(key, Seq(input))
  }

  def getCurrentState(
      key: EventEntityQuery[FeedUpdate]
  ): Future[Seq[FeedUpdate]] = {
    service.feedUpdateService.get(key)
  }
}
