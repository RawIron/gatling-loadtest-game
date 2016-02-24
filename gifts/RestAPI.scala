package loadtest.gifts

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._

import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.gifts.settings._


object RestAPI extends Headers
{
  val send_gift =
    http("send_gift")
      .post(Settings.SEND_GIFT)
      .param("user_id", "${user_id}")
      .param("skey", "${skey}")
      .param("to", "${user_id}")
      .param("data", "test_gift")
      .headers(headers_3)
      .check(status.is(200),
        jsonPath("$.success").is("true"),
        jsonPath("$.gift").exists.saveAs("gift")
        )

  val get_gifts =
    http("get_gifts")
      .post(Settings.GET_GIFTS)
      .param("user_id", "${user_id}")
      .param("skey", "${skey}")
      .headers(headers_3)
      .check(status.is(200),
        jsonPath("$.success").is("true")
        )

  val accept_gift =
    http("accept_gift")
      .post(Settings.ACCEPT_GIFT)
      .param("user_id", "${user_id}")
      .param("skey", "${skey}")
      .param("gift_id", "${gift}")
      .headers(headers_3)
      .check(status.is(200),
        jsonPath("$.success").is("true")
        )
}