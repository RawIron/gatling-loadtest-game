package loadtest.userflows

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
import loadtest.staticdata.{RestAPI => StaticDataRestAPI}


class FirstSessionSimulation extends Simulation
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


  val scn = scenario("First-Session")
    .feed(user_info_generator)
    .feed(gamestate_generator)
    .exec(session =>
      session.set("version_token", "1")
    )

    .tryMax(3) {
      exec(UserRestAPI.unregistered_login)
      .pause(0 milliseconds, 30 milliseconds)

      .exec(StaticDataRestAPI.get_static_data)
      .pause(20 milliseconds, 50 milliseconds)

      .exec(GameStateRestAPI.save_game)
      .pause(120 milliseconds, 150 milliseconds)

      .repeat(2) {
  	    randomSwitch (
  	      83.0 -> exec(GameStateRestAPI.save_game)
  	    )
  	    .pause(100 milliseconds, 140 milliseconds)

  	    .randomSwitch (
  	      47.0 -> exec(GameStateRestAPI.save_game)
  	    )
  	    .pause(60 milliseconds, 90 milliseconds)

  	    .randomSwitch (
  	      18.0 -> exec(GameStateRestAPI.save_game)
  	    )
  	    .pause(140 milliseconds, 260 milliseconds)
      }
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
