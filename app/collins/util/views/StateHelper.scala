package collins.util.views

import collins.models.{State, Status}
import play.api.libs.json.{Json, JsObject}
import play.api.templates.Html

object StateHelper {
  import State.StateFormat._
  def statesAsJson(): Html = {
    val anyState = State.findByAnyStatus()
    // create a map where keys are asset names and values are state objects
    val statusMap = Status.find().map { status =>
      val states = (State.findByStatus(status) ++ anyState).toSet.toList.sortWith((e1, e2) =>
        e1.name.compareTo(e2.name) < 0
      )
      status.name -> Json.toJson(states)
    }
    Html(Json.stringify(JsObject(statusMap)))
  }
}
