package loadtest.gamestate

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


class SaveAndLoadSimulation extends Simulation
                          with Headers
                          with UserInfoFeeder
                          with GameStateFeeder
{
  val httpConf = http
    .baseURL(LoadbalancerSettings.BASE_URL)
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val scn = scenario("game-state")
    .feed(user_info_generator)
    .feed(gamestate_generator)
    .exec(session =>
      session.set("version_token", "1")
    )

    .exec(UserRestAPI.unregistered_login)
    .pause(40 milliseconds, 120 milliseconds)

    .exec(RestAPI.save_game)
    .pause(40 milliseconds, 120 milliseconds)

    .exec(RestAPI.load_game)
    .pause(40 milliseconds, 120 milliseconds)


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