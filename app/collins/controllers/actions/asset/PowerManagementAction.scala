package collins.controllers.actions.asset

import collins.controllers.SecureController
import collins.power.PowerAction
import collins.controllers.forms._

import collins.util.security.SecuritySpecification

import play.api.data.Form
import play.api.data.Forms.{single, of}

case class PowerManagementAction(
  assetTag: String,
  spec: SecuritySpecification,
  handler: SecureController
) extends PowerManagementActionHelper(assetTag, spec, handler) {

  override lazy val powerAction: Option[PowerAction] = Form(single(
    "action" -> of[PowerAction]
  )).bindFromRequest()(request).fold(
    err => None,
    suc => Some(suc)
  )
}
