package org.huwtl.penfold.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import org.huwtl.penfold.domain.Job
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory

class HalStartedJobFormatter(selfLink: URI, jobLink: URI, completedJobLink: URI) {
  private val representationFactory = new DefaultRepresentationFactory

  def halFrom(job: Job) = {
    createHal(job).toString(HAL_JSON)
  }

  def halFrom(jobs: Iterable[Job]) = {
    val root = representationFactory.newRepresentation(selfLink)
    jobs.foreach(job => {
      root.withRepresentation("jobs", createHal(job))
    })
    root.toString(HAL_JSON)
  }

  def createHal(job: Job) = {
    representationFactory.newRepresentation(s"${selfLink.toString}/${job.id}")
      .withProperty("jobId", job.id)
      .withLink("job", s"${jobLink.toString}/${job.id}")
      .withLink("complete", s"${completedJobLink.toString}")
  }
}