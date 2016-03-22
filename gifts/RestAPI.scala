package loadtest.gifts

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._

import scala.concurrent.duration._

import loadtest.util._
import loadtest.gifts.settings._


object RestAPI extends Headers
{
  val send_gift =
    http("send_gift")
      .post(Settings.SEND_GIFT)
      .formParam("user_id", "${user_id}")
      .formParam("skey", "${skey}")
      .formParam("to", "${user_id}")
      .formParam("data", "test_gift")
      .headers(headers_3)
      .check(status.is(200),
        jsonPath("$.success").is("true"),
        jsonPath("$.gift").exists.saveAs("gift")
        )

  val get_gifts =
    http("get_gifts")
      .post(Settings.GET_GIFTS)
      .formParam("user_id", "${user_id}")
      .formParam("skey", "${skey}")
      .headers(headers_3)
      .check(status.is(200),
        jsonPath("$.success").is("true")
        )

  val accept_gift =
    http("accept_gift")
      .post(Settings.ACCEPT_GIFT)
      .formParam("user_id", "${user_id}")
      .formParam("skey", "${skey}")
      .formParam("gift_id", "${gift}")
      .headers(headers_3)
      .check(status.is(200),
        jsonPath("$.success").is("true")
        )
}
