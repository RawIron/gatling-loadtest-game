package loadtest.friends

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._

import loadtest.util._
import loadtest.settings._


class AddAndGetFriendsSimulation extends Simulation
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


  val scn = scenario("add-get-friends")
    .repeat(2) {
      feed(user_info_generator)
      .feed(facebook_user_generator)
      .exec(RestAPI.add_friend)
      .pause(40 milliseconds, 80 milliseconds)
    }
    .pause(140 milliseconds, 240 milliseconds)


    .feed(friend_list_generator)
    .exec(RestAPI.get_friends)
    .pause(0 milliseconds, 30 milliseconds)


  setUp(
    scn.inject(
    rampUsers(LoadSettings.BURST_USERS)
      over (LoadSettings.BURST_TIME seconds),
    rampUsersPerSec(LoadSettings.RAMP_USERS_START) to (LoadSettings.RAMP_USERS_END)
      during (LoadSettings.RAMP_TIME seconds),
    constantUsersPerSec(LoadSettings.ENDURANCE_USERS)
      during (LoadSettings.ENDURANCE_TIME seconds))
  ).protocols(httpConf)
}