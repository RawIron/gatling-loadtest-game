package loadtest.user

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._

import scala.util.parsing.json._
import loadtest.util._
import loadtest.settings._


class LoginSimulation extends Simulation
                      with Headers
                      with UserInfoFeeder
{
  val httpConf = http
    .baseURL(LoadbalancerSettings.BASE_URL)
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val scn = scenario("Users")
    .feed(user_info_generator)

    .exec(RestAPI.create_user)
    .pause(0 milliseconds, 10 milliseconds)

    .repeat(5) {
      exec(RestAPI.login)
      .pause(0 milliseconds, 10 milliseconds)

      .doIf("${response(2)}", "true") {
        exec(RestAPI.logout_using_parse)
      }
    }
    .pause(0 milliseconds, 10 milliseconds)


  setUp(
    scn.inject(
    ramp(LoadSettings.BURST_USERS users)
      over (LoadSettings.BURST_TIME seconds),
    rampRate (LoadSettings.RAMP_USERS_START usersPerSec) to (LoadSettings.RAMP_USERS_END usersPerSec)
      during (LoadSettings.RAMP_TIME seconds),
    constantRate(LoadSettings.ENDURANCE_USERS usersPerSec)
      during (LoadSettings.ENDURANCE_TIME seconds))
  ).protocols(httpConf)
}