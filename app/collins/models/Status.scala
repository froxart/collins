package collins.models

import play.api.libs.json.{Format, JsValue, JsSuccess, JsObject, Json, JsNumber, JsString}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import collins.models.shared.ValidatedEntity
import collins.models.shared.AnormAdapter



case class Status(name: String, description: String, id: Int = 0) extends ValidatedEntity[Int] {
  def getId(): Int = id
  override def validate() {
    require(name != null && name.length > 0, "Name must not be empty")
    require(description != null && description.length > 0, "Description must not be empty")
  }
  override def asJson: String =
    Json.stringify(Status.StatusFormat.writes(this))

  // We do this to mock the former Enum stuff
  override def toString(): String = name
}

object Status extends Schema with AnormAdapter[Status] {

  def Allocated = Status.findByName("Allocated")
  def Cancelled = Status.findByName("Cancelled")
  def Decommissioned = Status.findByName("Decommissioned")
  def Incomplete = Status.findByName("Incomplete")
  def Maintenance = Status.findByName("Maintenance")
  def New = Status.findByName("New")
  def Provisioning = Status.findByName("Provisioning")
  def Provisioned = Status.findByName("Provisioned")
  def Unallocated = Status.findByName("Unallocated")

  implicit object StatusFormat extends Format[Status] {
    override def reads(json: JsValue) = JsSuccess(Status(
      (json \ "NAME").as[String],
      (json \ "DESCRIPTION").as[String],
      (json \ "ID").as[Int]
    ))
    override def writes(status: Status) = JsObject(Seq(
      "ID" -> JsNumber(status.id),
      "NAME" -> JsString(status.name),
      "DESCRIPTION" -> JsString(status.description)
    ))
  }

  override val tableDef = table[Status]("status")
  on(tableDef)(s => declare(
    s.id is (autoIncremented,primaryKey),
    s.name is(unique)
  ))

  override protected def cacheKeys(s: Status) = Seq(
    "Status.find",
    "Status.findById(%d)".format(s.id),
    "Status.findByName(%s)".format(s.name.toLowerCase)
  )

  def find(): List[Status] = getOrElseUpdate("Status.find") {
    from(tableDef)(s => select(s)).toList
  }
  def findById(id: Int): Option[Status] = getOrElseUpdate("Status.findById(%d)".format(id)) {
    tableDef.lookup(id)
  }

  override def get(s: Status) = findById(s.id).get

  def findByName(name: String): Option[Status] = {
    getOrElseUpdate("Status.findByName(%s)".format(name.toLowerCase)) {
      tableDef.where(s =>
        s.name.toLowerCase === name.toLowerCase
      ).headOption
    }
  }

  override def delete(s: Status): Int = inTransaction {
    afterDeleteCallback(s) {
      tableDef.deleteWhere(p => p.id === s.id)
    }
  }

  def statusNames: Set[String] = find().map(_.name).toSet

}
