package loadtest.systems

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._

import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.settings._
import loadtest.systems.settings._


object RestAPI extends Headers
{
  val connection_check =
      http("connection_check")
        .get(LoadbalancerSettings.BASE_URL + Settings.CONNECTION_CHECK)
        .headers(headers_3)
        .check(status.is(200),
          jsonPath("$.success").is("true"))
}