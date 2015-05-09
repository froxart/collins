package collins.controllers.actions.asset

import collins.controllers.SecureController
import collins.controllers.Api

import collins.controllers.actions.SecureAction
import collins.controllers.actions.AssetAction
import collins.controllers.actions.RequestDataHolder

import collins.validation.StringUtil
import collins.models.{Asset, AssetLifecycle, Status => AssetStatus}
import collins.util.security.SecuritySpecification

import play.api.data.Form
import play.api.data.Forms.{optional, number}

case class DeleteAttributeAction(
  _assetTag: String,
  _attribute: String,
  spec: SecuritySpecification,
  handler: SecureController
) extends SecureAction(spec, handler) with AssetAction {

  case class ActionDataHolder(attribute: String, groupId: Option[Int]) extends RequestDataHolder

  lazy val groupId: Option[Int] = Form(
    "groupId" -> optional(number(0))
  ).bindFromRequest()(request).fold(
    err => None,
    suc => suc
  )

  override def validate(): Either[RequestDataHolder,RequestDataHolder] = {
    withValidAsset(_assetTag) { asset =>
      val trimmed = StringUtil.trim(_attribute)

      if (!trimmed.isDefined) {
        Left(RequestDataHolder.error400("attribute parameter must be specified"))
      } else {
        Right(ActionDataHolder(trimmed.get, groupId))
      }
    }
  }

  override def execute(rd: RequestDataHolder) = rd match {
    case adh@ActionDataHolder(attribute, gid) =>
      AssetLifecycle.updateAssetAttributes(definedAsset, mapForUpdates(adh)) match {
        case Left(throwable) =>
          handleError(RequestDataHolder.error500("Error deleting asset attributes"))
        case Right(status) =>
          Api.statusResponse(status, Status.Accepted)
      }
  }

  protected def mapForUpdates(adh: ActionDataHolder): Map[String,String] = {
    val a = Map(adh.attribute -> "")
    val b = adh.groupId.map(i => Map("groupId" -> i.toString)).getOrElse(Map.empty)
    a ++ b
  }

}
