package loadtest.scoreboards

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._

import loadtest.util._
import loadtest.settings._
import loadtest.user.{RestAPI => UserRestAPI}


class FriendsScoreSimulation extends Simulation
                  with Headers
                  with UserInfoFeeder
                  with ScoreFeeder
                  with FriendFeeder
{
  val httpConf = http
    .baseURL(LoadbalancerSettings.BASE_URL)
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val scn = scenario("Friends-Score")
    .feed(user_info_generator)
    .feed(score_generator)
    .feed(friend_generator)

    .tryMax(3) {
      exec(UserRestAPI.unregistered_login)
      .pause(0 milliseconds, 30 milliseconds)

      .exec(RestAPI.set_score)
      .pause(20 milliseconds, 50 milliseconds)

      .exec(RestAPI.get_friends_scores)
      .pause(120 milliseconds, 150 milliseconds)
    }


  setUp(
    scn.inject(
      rampUsers(LoadSettings.BURST_USERS)
        over (LoadSettings.BURST_TIME seconds),
      constantUsersPerSec(LoadSettings.ENDURANCE_USERS)
        during (LoadSettings.ENDURANCE_TIME seconds))
  )
  .protocols(httpConf)
}
