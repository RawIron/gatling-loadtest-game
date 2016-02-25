package loadtest.riak

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._


import loadtest.util._
import loadtest.settings.LoadSettings
import loadtest.riak.settings._
import loadtest.gamestate.GameStateFeeder


class GameStateSimulation extends Simulation
                      with Headers
                      with UserInfoFeeder
                      with GameStateFeeder
{
  val httpConf = http
    .baseURL(Settings.BASE_URL)
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val scn = scenario("riak game-state")
    .feed(user_info_generator)
    .feed(gamestate_generator)
    .exec(session => session.set("vclock", ""))

    .exec(RestAPI.save_game)
    .pause(0 milliseconds, 10 milliseconds)

    .repeat(5) {
      exec(RestAPI.save_game)
    }
    .pause(0 milliseconds, 20 milliseconds)

    .repeat(5) {
      exec(RestAPI.read_game)
    }
    .pause(0 milliseconds, 10 milliseconds)


  setUp(
    scn.inject(
    ramp(LoadSettings.BURST_USERS users) over (LoadSettings.BURST_TIME seconds),
    constantRate(LoadSettings.ENDURANCE_USERS usersPerSec) during (LoadSettings.ENDURANCE_TIME seconds))
  )
  .protocols(httpConf)
}