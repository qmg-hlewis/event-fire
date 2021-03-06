package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.domain.model.AggregateId

case class LastKnownPageDetails(id: AggregateId, sortValue: Long, direction: NavigationDirection)

sealed trait NavigationDirection

object NavigationDirection {
  case object Reverse extends NavigationDirection

  case object Forward extends NavigationDirection
}
