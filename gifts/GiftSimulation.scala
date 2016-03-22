package loadtest.gifts

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._


import loadtest.util._
import loadtest.settings._
import loadtest.user.{RestAPI => UserRestAPI}


class GiftSimulation extends Simulation
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


  val scn = scenario("Gift")
    .feed(user_info_generator)

    .tryMax(3) {
      exec(UserRestAPI.unregistered_login)
      .pause(0 milliseconds, 30 milliseconds)

      .exec(RestAPI.send_gift)
      .pause(20 milliseconds, 50 milliseconds)

      .exec(RestAPI.get_gifts)
      .pause(20 milliseconds, 50 milliseconds)

      .exec(RestAPI.accept_gift)
      .pause(20 milliseconds, 50 milliseconds)
    }


  setUp(
    scn.inject(
      rampUsers(LoadSettings.BURST_USERS)
        over (LoadSettings.BURST_TIME seconds),
      constantUsersPerSec(LoadSettings.ENDURANCE_USERS)
        during (LoadSettings.ENDURANCE_TIME seconds))
  ).protocols(httpConf)
}
