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
import loadtest.riak.test._


class StaticDataLoadSimulation extends Simulation
        with Headers
{
  val httpConf = http
    .baseURL(Settings.BASE_URL)
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
    .disableFollowRedirect


  val scn_loaddata = scenario("riak initial load")
    .exec(RestAPI.prepare_static_data)

  val scn = scenario("riak static data")
    .exec(RestAPI.read_static_data)
    .pause(0 milliseconds, 10 milliseconds)


  setUp(
    scn_loaddata.inject(
          ramp(1 users) over (1 seconds)),

    scn.inject(
    ramp(LoadSettings.BURST_USERS users) over (LoadSettings.BURST_TIME seconds),
    constantRate(LoadSettings.ENDURANCE_USERS usersPerSec) during (LoadSettings.ENDURANCE_TIME seconds))
  )
  .protocols(httpConf)
}