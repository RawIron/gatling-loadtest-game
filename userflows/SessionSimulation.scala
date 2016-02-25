package loadtest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.settings._
import loadtest.user.{RestAPI => UserRestAPI}
import loadtest.fakebook.{RestAPI => FakebookRestAPI}
import loadtest.facebook.{RestAPI => FacebookRestAPI}
import loadtest.gamestate.{RestAPI => GameStateRestAPI}
import loadtest.gamestate.GameStateFeeder
import loadtest.scoreboards.{RestAPI => ScoreboardRestAPI}
import loadtest.scoreboards.ScoreFeeder
import loadtest.staticdata.{RestAPI => StaticDataRestAPI}
import loadtest.systems.{RestAPI => SystemRestAPI}


class DroneFlowSimulation extends Simulation
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
    .exec(FakebookRestAPI.fakebook_me)


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

      .exec(FacebookRestAPI.facebook_connect)
      .pause(0 milliseconds, 10 milliseconds)

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
      .exec(FacebookRestAPI.facebook_login)
      .pause(0 milliseconds, 10 milliseconds)

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
    ramp(LoadSettings.BURST_USERS users)
      over (LoadSettings.BURST_TIME seconds),
    rampRate (LoadSettings.RAMP_USERS_START usersPerSec) to (LoadSettings.RAMP_USERS_END usersPerSec)
      during (LoadSettings.RAMP_TIME seconds),
    constantRate(LoadSettings.ENDURANCE_USERS usersPerSec)
      during (LoadSettings.ENDURANCE_TIME seconds))
    ).protocols(httpConf)
}