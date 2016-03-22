package loadtest.gamestate

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._

import scala.concurrent.duration._

import loadtest.util._
import loadtest.gamestate.settings._


object RestAPI extends Headers
               with ParseJsonResponse
{
  val save_game =
      http("save_game")
        .post(Settings.SAVE_GAME_URL)
        .formParam("user_id", "${user_id}")
        .formParam("skey", "${skey}")
        .formParam("version_token", "${version_token}")
        .formParam("game_state", "${gamestate}")
        .headers(headers_3)
        .check(
          status.is(200),
          jsonPath("$.success").is("true"),
          jsonPath("$.version_token").exists.saveAs("version_token")
          )

  val load_game =
      http("load_game")
        .get(Settings.LOAD_GAME_URL)
        .queryParam("user_id", "${user_id}")
        .queryParam("skey", "${skey}")
        .headers(headers_3)
        .check(
          status.is(200),
          jsonPath("$.success").is("true"),
          jsonPath("$.version_token").exists.saveAs("version_token"),
          jsonPath("$.game_content")
          .transformOption(
            gamestateMaybe => {
              val gamestate: Array[Byte] = gamestateMaybe match {
                case None => "".getBytes
                case Some(state) => state.getBytes
              }
              Option(MD5_Digest.md5SumString(gamestate))
            })
          .is("${gamestate_md5}"))

  val match_version =
      http("match_version")
        .post(Settings.MATCH_VERSION_URL)
        .formParam("user_id", "${user_id}")
        .formParam("skey", "${skey}")
        .formParam("version_token", "${version_token}")
        .headers(headers_3)
        .check(
          status.is(200),
          jsonPath("$.success").is("true")
          )
}