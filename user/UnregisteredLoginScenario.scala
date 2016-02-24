package loadtest.user

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._

import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.settings._



object UnregisteredLoginScenario
  extends Headers
  with UserInfoFeeder
{
  val scn = scenario("unregistered-login")
    .feed(user_info_generator)
    .exec(RestAPI.unregistered_login)
    .pause(0 milliseconds, 10 milliseconds)
}