package loadtest.friends

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._

import loadtest.util._
import loadtest.settings._


class LoadAndGetFriendsSimulation extends Simulation
                          with Headers
                          with UserInfoFeeder
                          with FacebookUserFeeder
{
  val httpConf = http
    .baseURL(LoadbalancerSettings.BASE_URL)
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val scn_load = scenario("load-friends")
    .repeat(1000) {
      feed(user_info_generator)
      .feed(facebook_user_generator)

      .exec(RestAPI.add_friend)
      .pause(40 milliseconds, 80 milliseconds)
    }
    .pause(400 milliseconds, 800 milliseconds)

  val scn_get = scenario("get-friends")
    .feed(friend_list_generator)
    .exec(RestAPI.get_friends)
    .pause(0 milliseconds, 30 milliseconds)


  setUp(
    scn_load.inject(
    rampUsers(100)
      over (1000 seconds)),

    scn_get.inject(
      rampUsers(LoadSettings.BURST_USERS)
        over (LoadSettings.BURST_TIME seconds),
      constantUsersPerSec(LoadSettings.ENDURANCE_USERS)
        during (LoadSettings.ENDURANCE_TIME seconds))
  ).protocols(httpConf)
}
