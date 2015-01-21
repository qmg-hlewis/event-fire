package org.huwtl.penfold.command

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.{AggregateVersion, Task, AggregateId}
import org.huwtl.penfold.domain.store.DomainRepository

class RequeueTaskHandlerTest extends Specification with Mockito {
  val expectedAggregateId = AggregateId("a1")

  val domainRepository = mock[DomainRepository]

  val startedTask = mock[Task]
  val requeuedTask = mock[Task]

  val commandDispatcher = new CommandDispatcherFactory(domainRepository, null).create

  "requeue task" in {
    domainRepository.getById[Task](expectedAggregateId) returns startedTask
    startedTask.requeue(AggregateVersion.init, None, None, None, None) returns requeuedTask

    commandDispatcher.dispatch(RequeueTask(expectedAggregateId, AggregateVersion.init, None, None, None, None))

    there was one(domainRepository).add(requeuedTask)
  }
}