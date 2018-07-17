package mesosphere.marathon
package metrics.current

sealed trait UnitOfMeasurement

object UnitOfMeasurement {
  case object None extends UnitOfMeasurement
  case object Memory extends UnitOfMeasurement
  case object Time extends UnitOfMeasurement
}
