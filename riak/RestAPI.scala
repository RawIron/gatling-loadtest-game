package loadtest.riak

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._

import scala.concurrent.duration._


import loadtest.util._
import loadtest.riak.test._
import loadtest.riak.settings._


object RestAPI extends Headers
               with ParseJsonResponse
{
  val create_user =
    http("create_user")
      .post("/buckets/users/keys/${username}?returnbody=true")
      .body(StringBody("""{
                  "username": "${username}",
                  "email": "${contactEmail}",
                  "vclock": ""
                }""")).asJSON
      .headers(headers_4)
      .check(status.in(200 to 204),
             header("X-Riak-Vclock").saveAs("vclock"))

  val update_user =
    http("update_user")
      .put("/buckets/users/keys/${username}?returnbody=true")
      .body(StringBody("""{
                  "username": "${username}",
                  "email": "${contactEmail}",
                  "vclock": "${vclock}"
                }""")).asJSON
      .headers(headers_4)
      .header("X-Riak-Vclock", "${vclock}")
      .check(status.in(200 to 204),
             header("X-Riak-Vclock").saveAs("vclock"))

  val read_user =
    http("read_user")
      .get("/buckets/users/keys/${username}")
      .headers(headers_2)
      .check(status.is(200),
             header("X-Riak-Vclock").saveAs("vclock"))


  val prepare_static_data =
    http("initial_load")
      .post("/buckets/gamecontent/keys/static?returnbody=true")
      .body(StringBody(TestData.gamecontent)).asJSON
      .headers(headers_4)
      .check(status.in(200 to 204),
             header("X-Riak-Vclock").saveAs("vclock"))

  val read_static_data =
    http("load_gamecontent")
      .get("/buckets/gamecontent/keys/static")
      .headers(headers_2)
      .check(status.is(200),
             header("X-Riak-Vclock").saveAs("vclock"))


  val save_game =
    http("save_game")
      .put("/buckets/gamestates/keys/${username}?returnbody=true")
      .body(StringBody("""{
                  "username": "${username}",
                  "email": "${contactEmail}",
                  "game_state": "${gamestate}",
                  "vclock": "${vclock}"
                }""")).asJSON
      .headers(headers_4)
      .header("X-Riak-Vclock", "${vclock}")
      .check(status.in(200 to 204),
             header("X-Riak-Vclock").saveAs("vclock"))

  val read_game =
    http("load_game")
      .get("/buckets/gamestates/keys/${username}")
      .headers(headers_2)
      .check(status.is(200),
             header("X-Riak-Vclock").saveAs("vclock"))
}
