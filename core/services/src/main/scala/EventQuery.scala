package tpm.services

case class EventQuery[T](
    entity: T,
    limit: Option[Int] = None,
    offset: Option[Int] = None
)
