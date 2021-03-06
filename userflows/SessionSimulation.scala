package loadtest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._

import loadtest.util._
import loadtest.settings._
import loadtest.user.{RestAPI => UserRestAPI}
import loadtest.gamestate.{RestAPI => GameStateRestAPI}
import loadtest.gamestate.GameStateFeeder
import loadtest.scoreboards.{RestAPI => ScoreboardRestAPI}
import loadtest.scoreboards.ScoreFeeder
import loadtest.staticdata.{RestAPI => StaticDataRestAPI}
import loadtest.systems.{RestAPI => SystemRestAPI}


class SessionSimulation extends Simulation
                  with Headers
                  with UserInfoFeeder
                  with GameStateFeeder
                  with ScoreFeeder
{
  val httpConf = http
    .baseURL(LoadbalancerSettings.BASE_URL)
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val scn = scenario("First-Session")
    .feed(user_info_generator)
    .exec(session =>
      session.set("version_token", "1")
    )

    .feed(access_token_generator)

    .tryMax(3) {
      // First Session
      exec(SystemRestAPI.connection_check)
      .pause(0 milliseconds, 30 milliseconds)

      .exec(StaticDataRestAPI.get_static_data)
      .pause(500 milliseconds, 5000 milliseconds)

      .exec(UserRestAPI.unregistered_login)
      .pause(120 milliseconds, 150 milliseconds)

      .feed(gamestate_generator)
      .exec(GameStateRestAPI.save_game)

      // Break between Sessions
      .pause(1 seconds, 30 seconds)

      // Second Session
      .exec(SystemRestAPI.connection_check)
      .pause(0 milliseconds, 30 milliseconds)

      .exec(StaticDataRestAPI.get_static_data)
      .pause(500 milliseconds, 5000 milliseconds)

      .exec(GameStateRestAPI.match_version)
      .pause(120 milliseconds, 150 milliseconds)

      .feed(gamestate_generator)
      .exec(GameStateRestAPI.save_game)

      // Break between Sessions
      .pause(1 seconds, 30 seconds)

      // Third Session
      .exec(SystemRestAPI.connection_check)
      .pause(0 milliseconds, 30 milliseconds)

      .exec(StaticDataRestAPI.get_static_data)
      .pause(500 milliseconds, 5000 milliseconds)

      .exec(GameStateRestAPI.match_version)
      .pause(120 milliseconds, 150 milliseconds)

      .feed(gamestate_generator)
      .exec(GameStateRestAPI.save_game)
      .pause(120 milliseconds, 150 milliseconds)

      .exec(ScoreboardRestAPI.get_scores)
      .pause(120 milliseconds, 150 milliseconds)
      .exec(ScoreboardRestAPI.get_scores)
      .pause(120 milliseconds, 150 milliseconds)

      .feed(score_generator)
      .exec(ScoreboardRestAPI.set_score)
      .pause(20 milliseconds, 50 milliseconds)

      .feed(gamestate_generator)
      .exec(GameStateRestAPI.save_game)
      .pause(120 milliseconds, 150 milliseconds)

      // Break between Sessions
      .pause(1 seconds, 30 seconds)

      // Fourth Session
      .exec(StaticDataRestAPI.get_static_data)
      .pause(500 milliseconds, 5000 milliseconds)

      .exec(GameStateRestAPI.match_version)
      .pause(120 milliseconds, 150 milliseconds)

      .feed(gamestate_generator)
      .exec(GameStateRestAPI.save_game)
      .pause(120 milliseconds, 150 milliseconds)

      .feed(gamestate_generator)
      .exec(GameStateRestAPI.save_game)
      .pause(120 milliseconds, 150 milliseconds)

      .exec(ScoreboardRestAPI.get_scores)
      .pause(120 milliseconds, 150 milliseconds)
      .exec(ScoreboardRestAPI.get_scores)
      .pause(120 milliseconds, 150 milliseconds)

      .feed(score_generator)
      .exec(ScoreboardRestAPI.set_score)
      .pause(20 milliseconds, 50 milliseconds)

      .feed(gamestate_generator)
      .exec(GameStateRestAPI.save_game)
      .pause(120 milliseconds, 150 milliseconds)
    }


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
