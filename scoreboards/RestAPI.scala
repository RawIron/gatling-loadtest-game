package loadtest.scoreboards

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._

import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.scoreboards.settings._


object RestAPI extends Headers
{
  val set_score =
    http("set_score")
      .post(Settings.SET_SCORE)
      .param("user_id", "${user_id}")
      .param("skey", "${skey}")
      .param("scoreboard", "test_scoreboard")
      .param("score", "${score}")
      .headers(headers_3)
      .check(status.is(200),
        jsonPath("$.success").is("true")
        )

  val get_scores =
    http("get_scores")
      .post(Settings.GET_SCORES)
      .param("user_id", "${user_id}")
      .param("skey", "${skey}")
      .param("scoreboard", "test_scoreboard")
      .headers(headers_3)
      .check(
        status.is(200),
        jsonPath("$.success").is("true")
      )

  val get_friends_scores =
    http("get_friend_scores")
      .post(Settings.GET_FRIENDS_SCORES)
      .param("user_id", "${user_id}")
      .param("skey", "${skey}")
      .param("scoreboard", "test_scoreboard")
      .param("friends", "${friends}")
      .headers(headers_3)
      .check(
        status.is(200),
        jsonPath("$.success").is("true")
      )
}