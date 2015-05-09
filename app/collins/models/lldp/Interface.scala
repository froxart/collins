package collins.models.lldp

import play.api.libs.json.{Format, JsSuccess, JsObject, Json, JsValue}

object Interface {
  import Chassis._
  import Port._
  import Vlan._
  implicit object InterfaceFormat extends Format[Interface] {
    override def reads(json: JsValue) = JsSuccess(Interface(
      (json \ "NAME").as[String],
      (json \ "CHASSIS").as[Chassis],
      (json \ "PORT").as[Port],
      (json \ "VLANS").as[Seq[Vlan]]
    ))
    override def writes(iface: Interface) = JsObject(Seq(
      "NAME" -> Json.toJson(iface.name),
      "CHASSIS" -> Json.toJson(iface.chassis),
      "PORT" -> Json.toJson(iface.port),
      "VLANS" -> Json.toJson(iface.vlans)
    ))
  }
}

case class Interface(
  name: String, chassis: Chassis, port: Port, vlans: Seq[Vlan]
) extends LldpAttribute {

  import Interface._

  def isPortLocal: Boolean = port.isLocal
  def portAsInt: Int = port.toInt
  def macAddress: String = chassis.macAddress

  override def toJsValue() = Json.toJson(this)
}
