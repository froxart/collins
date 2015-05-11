package collins.controllers.actions.logs

import collins.controllers.SecureController
import collins.controllers.ResponseData

import collins.controllers.actions.SecureAction
import collins.controllers.actions.AssetAction
import collins.controllers.actions.RequestDataHolder

import collins.models.{Asset, AssetLog}
import collins.models.shared.{Page, PageParams}
import collins.models.conversions.AssetLogFormat
import collins.util.security.SecuritySpecification
import play.api.libs.json.{Json, JsObject}

case class FindAction(
  assetTag: Option[String],
  pageParams: PageParams,
  filter: String,
  spec: SecuritySpecification,
  handler: SecureController
) extends SecureAction(spec, handler) with AssetAction {

  case class ActionDataHolder(asset: Option[Asset], params: PageParams, filter: String) extends RequestDataHolder

  override def validate(): Validation = {
    assetTag.map { a =>
      withValidAsset(a) { asset =>
        Right(ActionDataHolder(Some(asset), pageParams, filter))
      }
    }.getOrElse(Right(ActionDataHolder(None, pageParams, filter)))
  }

  override def execute(rd: RequestDataHolder) = rd match {
    case adh@ActionDataHolder(asset, params, filter) =>
      val logs = getLogs(adh)
      val pageMap = getPaginationMap(logs)
      ResponseData(Status.Ok, JsObject(pageMap ++ Seq(
        "Data" -> Json.toJson(logs.items)
      )), logs.getPaginationHeaders)
  }

  protected def getLogs(adh: ActionDataHolder): Page[AssetLog] = {
    val ActionDataHolder(asset, params, filter) = adh
    AssetLog.list(asset, params.page, params.size, params.sort.toString, filter)
  }

  protected def getPaginationMap(logs: Page[AssetLog]) = logs.getPaginationJsObject

}
