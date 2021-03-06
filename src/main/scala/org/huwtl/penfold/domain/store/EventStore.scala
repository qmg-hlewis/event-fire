package org.huwtl.penfold.domain.store

import org.huwtl.penfold.app.support.ConnectivityCheck
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.event.Event

trait EventStore extends ConnectivityCheck {
  def retrieveBy(id: AggregateId): List[Event]

  def add(event: Event): Event
}
