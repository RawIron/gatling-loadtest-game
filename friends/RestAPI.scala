package loadtest.friends

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._

import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.friends.settings._


object RestAPI extends Headers
{
  val add_friend =
    http("add_friend")
      .post(Settings.ADD_FRIEND)
      .param("user", "${username}")
      .param("facebook", "${facebook_user}")
      .param("name", "${facebook_name}")
      .headers(headers_3)
      .check(
          status.is(200),
          jsonPath("$.success").is("true"),
          jsonPath("$.user_added").is("true")
          )

  val get_friends =
    http("get_friends")
      .post(Settings.GET_FRIENDS)
      .param("facebook_ids", "${my_friends}")
      .headers(headers_3)
      .check(
        status.is(200),
        jsonPath("$.success").is("true"),
        jsonPath("$.friends").exists
        )

  val get_friends_json_array_body =
    http("get_friends")
      .post(Settings.GET_FRIENDS)
      .body(StringBody("${my_friends}")).asJSON
      .headers(headers_3)
      .check(
        status.is(200),
        jsonPath("$.success").is("true"),
        jsonPath("$.friends").exists
        )

  val get_friends_multi_value =
    http("get_friends")
      .post(Settings.GET_FRIENDS)
      .multiValuedQueryParam("facebook_ids", "${my_friends}")
      .headers(headers_3)
      .check(
        status.is(200),
        jsonPath("$.success").is("true"),
        jsonPath("$.friends").exists
        )
}