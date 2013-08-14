package com.hlewis.eventfire.app.store

import scala.collection.mutable
import com.hlewis.eventfire.domain._
import com.hlewis.eventfire.domain.Job
import com.hlewis.eventfire.domain.Cron

class InMemoryJobStore extends JobStore {
  private val store = mutable.LinkedHashMap[String, Job](
    "job1" -> Job("job1", "test", Some(Cron("0", "*", "*", "*", "*", "*")), None, "waiting", Payload(Map("data" -> "value"))),
    "job2" -> Job("job2", "test", Some(Cron("0", "*", "*", "*", "*", "*")), None, "waiting", Payload(Map("data" -> "value")))
  )

  override def retrieveBy(id: String) = {
    store.get(id)
  }

  override def triggerPendingJobs() {
    store.values
      .filter(!_.nextTriggerDate.isAfterNow)
      .filter(_.status == "waiting")
      .foreach(job => store.put(job.id, Job(job.id, job.jobType, job.cron, job.triggerDate, "triggered", job.payload)))
  }

  override def add(job: Job) = {
    store.put(job.id, job)
    job
  }

  override def update(job: Job) = {
    store.put(job.id, job)
    job
  }

  override def remove(job: Job) {
    store.remove(job.id)
  }

  override def retrieve(status: String) = {
    store.values
      .filter(_.status == status)
      .toList
      .sortWith((job1, job2) => job1.nextTriggerDate.isAfter(job2.nextTriggerDate))
  }
}