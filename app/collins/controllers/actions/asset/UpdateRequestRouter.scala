package collins.controllers.actions.asset

import collins.controllers.actions.SecureAction
import collins.models.AssetMeta.Enum.{ChassisTag, RackPosition}

import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Action

/**
 * Determines if a request is for modifying status or not
 */
object UpdateRequestRouter {

  sealed trait Matcher
  object Matcher {
    object StatusOnly extends Matcher
    object SomethingSomethingDarkSide extends Matcher
  }

  def getMatchType(req: Request[AnyContent]): Matcher = {
    val map = ActionAttributeHelper.getInputMapFromRequest(req)
    val nonStatus = Set("lshw", "lldp", ChassisTag.toString, RackPosition.toString, "groupId", "attribute")
    val foundNonStatus = map.filter(kv => nonStatus.contains(kv._1)).size > 0
    if (foundNonStatus) {
      Matcher.SomethingSomethingDarkSide
    } else {
      Matcher.StatusOnly
    }
  }

  def apply(matcher: PartialFunction[Matcher,SecureAction]): Action[AnyContent] = Action { implicit req =>
    matcher(getMatchType(req))(req)
  }
}
