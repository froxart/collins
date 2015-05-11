package collins.controllers.actors

import collins.controllers.ResponseData
import collins.controllers.Api
import scala.concurrent.duration._
import collins.models.{Asset, AssetLifecycle, MetaWrapper, Status => AStatus}
import play.api.mvc.{AnyContent, Request}
import collins.util.plugins.SoftLayer
import collins.util.concurrent.BackgroundProcess
import scala.concurrent.{Await, ExecutionContext}
import collins.softlayer.SoftLayerConfig
import java.util.concurrent.TimeUnit

case class AssetCancelProcessor(tag: String, userTimeout: Option[FiniteDuration] = None)(implicit req: Request[AnyContent])
  extends BackgroundProcess[Either[ResponseData,Long]]
{
  val timeout = userTimeout.getOrElse(Duration(SoftLayerConfig.cancelRequestTimeoutMs, TimeUnit.MILLISECONDS))

  private val noReason : Either[ResponseData,Long] = Left(Api.getErrorMessage("No reason specified for cancellation"))

  private val softlayerDisabled : Either[ResponseData,Long] = Left(Api.getErrorMessage("SoftLayer plugin is not enabled"))

  private val nonSoftlayerAsset : Either[ResponseData,Long] = Left(Api.getErrorMessage("Asset is not a softlayer asset"))

  private val cancellationError : Either[ResponseData,Long] = Left(Api.getErrorMessage("There was an error cancelling this server"))

  def run(): Either[ResponseData,Long] = {

    val reasonOpt = req.body.asFormUrlEncoded.flatMap(_.get("reason")).flatMap(_.headOption).map(_.trim).filter(_.size > 0)

    reasonOpt.fold(noReason){ r =>
      val reason = r.trim
      Api.withAssetFromTag(tag) { asset =>
        SoftLayer.pluginEnabled.fold(softlayerDisabled){ plugin =>

          plugin.softLayerId(asset).fold(nonSoftlayerAsset) { n =>

            Await.result(plugin.cancelServer(n, reason), timeout) match  {
              case 0L => cancellationError
              case ticketId =>
                Asset.inTransaction {
                  MetaWrapper.createMeta(asset, Map("CANCEL_TICKET" -> ticketId.toString))
                  AssetLifecycle.updateAssetStatus(asset, AStatus.Cancelled, None, reason)
                }
                Await.result(plugin.setNote(n, "Cancelled: %s".format(reason)), timeout)
                Right(ticketId)
            }
          }
        }
      }
    }
  }
}
