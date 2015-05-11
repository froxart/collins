package collins.provisioning

import play.api.Logger

import collins.models.Asset
import collins.shell.CommandResult

trait Provisioner {
  protected[this] val logger = Logger(getClass)
  def profiles: Set[ProvisionerProfile]
  def canProvision(asset: Asset): Boolean
  def provision(request: ProvisionerRequest): CommandResult
  def test(request: ProvisionerRequest): CommandResult
  def profile(id: String): Option[ProvisionerProfile] = {
    profiles.find(_.identifier == id)
  }
  def makeRequest(token: String, id: String, notification: Option[String] = None, suffix: Option[String] = None): Option[ProvisionerRequest] = {
    profile(id).map { p =>
      ProvisionerRequest(token, p, notification, suffix)
    }
  }
}
