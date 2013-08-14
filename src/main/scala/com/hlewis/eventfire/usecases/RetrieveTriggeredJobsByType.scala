package com.hlewis.eventfire.usecases

import com.hlewis.eventfire.domain.JobStore

class RetrieveTriggeredJobsByType(jobStore: JobStore) {
  def retrieve(jobType: String) = {
    jobStore.retrieve("triggered").filter(_.jobType == jobType)
  }
}