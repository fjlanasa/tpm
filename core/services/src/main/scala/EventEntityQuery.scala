package tpm.services

case class EventEntityQuery[T](
    entity: T,
    limit: Option[Int] = None,
    offset: Option[Int] = None
)
