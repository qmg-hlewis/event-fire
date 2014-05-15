package org.huwtl.penfold.domain.model

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.event._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.event.TaskCreated
import org.huwtl.penfold.domain.event.TaskTriggered
import org.huwtl.penfold.domain.event.TaskStarted
import org.huwtl.penfold.domain.exceptions.AggregateConflictException
import org.huwtl.penfold.domain.model.patch.Patch

class TaskTest extends Specification {

  val queue = QueueId("abc")

  "create new task" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCreated]))
  }

  "create new future task" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[FutureTaskCreated]))
  }

  "trigger new future task if trigger date in past" in {
    val createdTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().minusDays(1), Payload.empty, None)
    typesOf(createdTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
  }

  "trigger future task" in {
    val readyTask = Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger()
    typesOf(readyTask.uncommittedEvents) must beEqualTo(List(classOf[TaskTriggered], classOf[FutureTaskCreated]))
  }

  "ensure only waiting tasks can be triggered" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).trigger() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).trigger().trigger() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().trigger() must throwA[RuntimeException]
  }

  "start task" in {
    val startedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start()
    typesOf(startedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure only ready tasks can be started" in {
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).start() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().start() must throwA[RuntimeException]
  }

  "cancel task" in {
    val cancelledTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).cancel()
    typesOf(cancelledTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCancelled], classOf[TaskCreated]))
  }

  "complete task" in {
    val completedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().complete()
    typesOf(completedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskCompleted], classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure only started tasks can be completed" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).complete() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().cancel().complete() must throwA[RuntimeException]
  }

  "requeue task" in {
    val requeuedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).start().requeue()
    typesOf(requeuedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskRequeued], classOf[TaskStarted], classOf[TaskCreated]))
  }

  "ensure waiting, ready, archived tasks cannot be requeued" in {
    Task.create(AggregateId("1"), QueueBinding(queue), DateTime.now().plusHours(1), Payload.empty, None).requeue() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).requeue() must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive().requeue() must throwA[RuntimeException]
  }

  "update task payload" in {
    val updatedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).updatePayload(AggregateVersion.init, Patch(Nil), None, None)
    typesOf(updatedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskPayloadUpdated], classOf[TaskCreated]))
  }

  "prevent concurrent task payload updates"in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None)
      .updatePayload(AggregateVersion.init, Patch(Nil), None, None)
      .updatePayload(AggregateVersion.init, Patch(Nil), None, None) must throwA[AggregateConflictException]
  }

  "ensure completed, cancelled, archived tasks cannot accept updated payload" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).complete().updatePayload(AggregateVersion.init, Patch(Nil), None, None) must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).cancel().updatePayload(AggregateVersion.init.next, Patch(Nil), None, None) must throwA[RuntimeException]
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive().updatePayload(AggregateVersion.init.next, Patch(Nil), None, None) must throwA[RuntimeException]
  }

  "archive task" in {
    val archivedTask = Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive()
    typesOf(archivedTask.uncommittedEvents) must beEqualTo(List(classOf[TaskArchived], classOf[TaskCreated]))
  }

  "ensure cannot archive an already archived task" in {
    Task.create(AggregateId("1"), QueueBinding(queue), Payload.empty, None).archive().archive() must throwA[RuntimeException]
  }

  private def typesOf(events: List[Event]) = {
    events.map(_.getClass)
  }
}
