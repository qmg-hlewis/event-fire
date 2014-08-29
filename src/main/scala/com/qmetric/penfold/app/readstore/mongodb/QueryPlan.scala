package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.readstore.Filter

case class QueryPlan(restrictionFields: List[RestrictionField], sortFields: List[SortField])

case class RestrictionField(path: String, filter: Filter)

case class SortField(path: String)