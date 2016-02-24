package loadtest.util

import io.gatling.core.Predef._
import scala.util.parsing.json._
import java.security.MessageDigest


trait Headers
{
  val headers_1 = Map(
    "Keep-Alive" -> "115")

  val headers_2 = Map(
    "Accept" -> "application/json, text/javascript, */*; q=0.01",
    "Keep-Alive" -> "5",
    "Content-Type" -> "text/plain")

  val headers_3 = Map(
    "Accept" -> "application/json, text/javascript, */*; q=0.01",
    "Keep-Alive" -> "5",
    "Content-Type" -> "application/x-www-form-urlencoded")

  val headers_4 = Map(
    "Accept" -> "application/json, text/javascript, */*; q=0.01",
    "Keep-Alive" -> "5",
    "Content-Type" -> "application/json")

  val headers_6 = Map(
    "Accept" -> "application/json, text/javascript, */*; q=0.01",
    "Keep-Alive" -> "115",
    "X-Requested-With" -> "XMLHttpRequest")
}


trait UserInfoFeeder
{
  val user_info_from_csv = csv("user_information.csv").queue

  val access_token_generator = new Feeder[String] {
    // always return true as this feeder can be polled infinitively
    override def hasNext = true

    override def next: Map[String, String] = {
      val access_token = scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits).toString

      Map("access_token" -> access_token)
    }
  }

  val user_info_generator = new Feeder[String] {
    import org.joda.time.DateTime

    import scala.util.Random
    private val RNG = new Random
    // random number in between [a...b]
    private def randInt(a:Int, b:Int) = RNG.nextInt(b-a) + a

    private def daysOfMonth(year:Int, month:Int) = new DateTime(year, month, 1, 0, 0, 0, 000).dayOfMonth.getMaximumValue

    // always return true as this feeder can be polled infinitively
    override def hasNext = true

    override def next: Map[String, String] = {
      val username = scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits).toString
      val email = username + "_loadtest@dontsend.com"
      val year = randInt(1945, 1994)
      val month = randInt(1, 12)
      val day = randInt(1, daysOfMonth(year, month))

      Map("username" -> username,
          "contactEmail" -> email,
          "birthdayYear" -> year.toString,
          "birthdayMonth" -> month.toString,
          "birthdayDay" -> day.toString,
          "password" -> "cannotbeguessed2112")
      }
  }
}


object MD5_Digest
{
  def md5SumString(bytes : Array[Byte]) : String = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)

    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }

  def md5(s: String) = {
      MessageDigest.getInstance("MD5").digest(s.getBytes)
  }
}


trait ParseJsonResponse
{
  def parseCredentialsFromResponse(body: String) : List[Any] = {
    val json:Option[Any] = JSON.parseFull(body)
    val response:Map[String,Any] = json.get.asInstanceOf[Map[String, Any]]

    val success:Boolean = response.get("success").get.asInstanceOf[Boolean]
    if (success) {
      val user_id:String = response.get("user_id").get.asInstanceOf[Double].toInt.toString
      val skey:String = response.get("skey").get.asInstanceOf[String]
      List(user_id.toInt, skey, success)
    } else {
      List()
    }
  }

  def parseSuccessFromResponse(body: String) : Boolean = {
    val json:Option[Any] = JSON.parseFull(body)
    val response:Map[String,Any] = json.get.asInstanceOf[Map[String, Any]]

    val success:Boolean = response.get("success").get.asInstanceOf[Boolean]
    success
  }
}