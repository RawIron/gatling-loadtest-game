package loadtest.riak

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._
import loadtest.settings._


class RiakUserSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://ip-172-31-21-253.us-west-2.compute.internal:8098")
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val headers_1 = Map(
    "Keep-Alive" -> "115")

  val headers_2 = Map(
    "Accept" -> "application/json, text/javascript, */*; q=0.01",
    "Keep-Alive" -> "5",
    "Content-Type" -> "text/plain")

  val headers_3 = Map(
    "Keep-Alive" -> "115",
    "Content-Type" -> "application/x-www-form-urlencoded")

  val headers_4 = Map(
    "Accept" -> "application/json, text/javascript, */*; q=0.01",
    "Keep-Alive" -> "5",
    "Content-Type" -> "application/json")

  val headers_6 = Map(
    "Accept" -> "application/json, text/javascript, */*; q=0.01",
    "Keep-Alive" -> "115",
    "X-Requested-With" -> "XMLHttpRequest")


  val user_info_from_csv = csv("user_information.csv").queue

  val user_info_generator = new Feeder[String] {
    import org.joda.time.DateTime
    import scala.util.Random

    private val RNG = new Random

    // random number in between [a...b]
    private def randInt(a:Int, b:Int) = RNG.nextInt(b-a) + a

    private def daysOfMonth(year:Int, month:Int) = new DateTime(year, month, 1, 0, 0, 0, 0).dayOfMonth.getMaximumValue

    // always return true as this feeder can be polled infinitively
    override def hasNext = true

    override def next: Map[String, String] = {
      val email = scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits) + "_loadtest@dontsend.com"
      val year = randInt(1945, 1994)
      val month = randInt(1, 12)
      val day = randInt(1, daysOfMonth(year, month))

      Map("username" -> email,
          "contactEmail" -> email,
          "birthdayYear" -> year.toString,
          "birthdayMonth" -> month.toString,
          "birthdayDay" -> day.toString)
      }
  }


  val scn = scenario("Users")
    .feed(user_info_generator)

    .exec(
      http("create_user")
        .post("/buckets/users/keys/${username}?returnbody=true")
        .body(StringBody("""{
                    "username": "${username}",
                    "email": "${contactEmail}",
                    "vclock": ""
                  }""")).asJSON
        .headers(headers_4)
        .check(status.in(200 to 204),
               header("X-Riak-Vclock").saveAs("vclock")))
    .pause(0 milliseconds, 10 milliseconds)

    .repeat(5) {
    exec(
      http("update_user")
        .put("/buckets/users/keys/${username}?returnbody=true")
        .body(StringBody("""{
                    "username": "${username}",
                    "email": "${contactEmail}",
                    "vclock": "${vclock}"
                  }""")).asJSON
        .headers(headers_4)
        .header("X-Riak-Vclock", "${vclock}")
        .check(status.in(200 to 204),
               header("X-Riak-Vclock").saveAs("vclock")))
    }
    .pause(0 milliseconds, 20 milliseconds)

    .repeat(5) {
    exec(
      http("read_user")
        .get("/buckets/users/keys/${username}")
        .headers(headers_2)
        .check(status.is(200),
               header("X-Riak-Vclock").saveAs("vclock")))
    }
    .pause(0 milliseconds, 10 milliseconds)


  setUp(
    scn.inject(
    rampUsers(LoadSettings.BURST_USERS)
      over (LoadSettings.BURST_TIME seconds),
    constantUsersPerSec(LoadSettings.ENDURANCE_USERS)
      during (LoadSettings.ENDURANCE_TIME seconds))
  ).protocols(httpConf)
}
