package org.huwtl.penfold.app.schedule

import java.util.concurrent.TimeUnit._

import org.huwtl.penfold.app.StartedTaskTimeoutConfiguration
import org.huwtl.penfold.command.CommandDispatcher
import org.huwtl.penfold.domain.model.Status.Started
import org.huwtl.penfold.readstore.{ReadStore, TaskProjectionReference}
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

import scala.concurrent.duration.FiniteDuration

class StartedTaskTimeoutSchedulerTest extends SpecificationWithJUnit with Mockito {

  "requeue started tasks on timeout" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val config = StartedTaskTimeoutConfiguration(FiniteDuration(1L, MINUTES))

    new StartedTaskTimeoutScheduler(readStore, commandDispatcher, config).process()

    there was one(readStore).forEachTimedOutTask(===(Started), ===(FiniteDuration(1L, MINUTES)), any[TaskProjectionReference => Unit])
  }
}
