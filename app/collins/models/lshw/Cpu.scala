package collins.models.lshw

import play.api.libs.json.{Format, JsSuccess, JsObject, Json, JsValue}

object Cpu {

  implicit object CpuFormat extends Format[Cpu] {
    override def reads(json: JsValue) = JsSuccess(Cpu(
      (json \ "CORES").as[Int],
      (json \ "THREADS").as[Int],
      (json \ "SPEED_GHZ").as[Double],
      (json \ "DESCRIPTION").as[String],
      (json \ "PRODUCT").as[String],
      (json \ "VENDOR").as[String]
    ))
    override def writes(cpu: Cpu) = JsObject(Seq(
      "CORES" -> Json.toJson(cpu.cores),
      "THREADS" -> Json.toJson(cpu.threads),
      "SPEED_GHZ" -> Json.toJson(cpu.speedGhz),
      "DESCRIPTION" -> Json.toJson(cpu.description),
      "PRODUCT" -> Json.toJson(cpu.product),
      "VENDOR" -> Json.toJson(cpu.vendor)
    ))
  }
}

case class Cpu(
  cores: Int, threads: Int, speedGhz: Double, description: String, product: String, vendor: String
) extends LshwAsset {
  import Cpu._
  override def toJsValue() = Json.toJson(this)
}
