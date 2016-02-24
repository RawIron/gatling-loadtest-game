package loadtest.staticdata

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._

import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.settings._
import loadtest.staticdata.settings._


object RestAPI extends Headers
{
  val get_static_data =
      http("static_data")
        .get(LoadbalancerSettings.BASE_URL + Settings.READ_STATIC_URL)
        .headers(headers_3)
        .check(status.is(200),
          jsonPath("$.success").is("true"))

  val get_content_data =
      http("content_data")
        .get(LoadbalancerSettings.BASE_URL + Settings.READ_CONTENT_URL)
        .headers(headers_3)
        .check(status.is(200),
          jsonPath("$.success").is("true"))
}