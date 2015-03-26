package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.store.DomainRepository
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.domain.model.AggregateId

class CreateTaskHandlerTest extends Specification with Mockito {

  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val aggregateIdFactory = mock[AggregateIdFactory]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, aggregateIdFactory).create

  "create task" in {
    aggregateIdFactory.create returns expectedAggregateId

    val aggregateId = commandDispatcher.dispatch(new CreateTask(QueueId("q1"), Payload.empty, None))

    there was one(domainRepository).add(any[Task])
    aggregateId must beEqualTo(expectedAggregateId)
  }
}
