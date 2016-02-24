package loadtest.user

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._

import scala.concurrent.duration._
import bootstrap._
import assertions._

import loadtest.util._
import loadtest.user.settings._


object RestAPI extends Headers
               with ParseJsonResponse
{
  val create_user =
      http("create_user")
        .post(Settings.USER_CREATE)
        .param("username", "${username}")
        .param("email", "${contactEmail}")
        .param("password", "${password}")
        .param("password_confirm", "${password}")
        .headers(headers_3)
        .check(status.is(200),
            bodyString.transform(
            bodyMaybe => {
              Option( bodyMaybe match {
                case None => false
                case Some(body) => parseSuccessFromResponse(body)
              })
            }
            ).is(true)
        )

  val unregistered_login =
      http("unregistered_login")
        .post(Settings.USER_LOGIN_UNREGISTERED)
        .param("uid", "${username}")
        .headers(headers_3)
        .check(status.is(200),
            jsonPath("$.success").is("true").saveAs("success"),
            jsonPath("$.user_id").exists.saveAs("user_id"),
            jsonPath("$.skey").exists.saveAs("skey"))

  val login =
      http("login_user")
        .post(Settings.USER_LOGIN)
        .param("username", "${username}")
        .param("password", "${password}")
        .headers(headers_3)
        .check(status.is(200),
            bodyString.transform(
            bodyMaybe => {
              Option( bodyMaybe match {
                case None => List()
                case Some(body) => parseCredentialsFromResponse(body)
              })
            }
            ).exists.saveAs("response"),
            jsonPath("$.success").is("true"))

  val logout =
      http("logout_user")
        .post(Settings.USER_LOGOUT)
        .param("user_id", "${user_id}")
        .param("skey", "${skey}")
        .headers(headers_3)
        .check(status.is(200),
          jsonPath("$.success").is("true"))

  val logout_using_parse =
       http("logout_user")
        .post(Settings.USER_LOGOUT)
        .param("user_id", "${response(0)}")
        .param("skey", "${response(1)}")
        .headers(headers_3)
        .check(status.is(200),
          bodyString.transform(
          bodyMaybe => {
            Option( bodyMaybe match {
              case None => false
              case Some(body) => parseSuccessFromResponse(body)
            })
          }
          ).is(true))

  val user_connect =
      http("connect_account")
        .post(Settings.USER_CONNECT)
        .param("user_id", "${user_id}")
        .param("skey", "${skey}")
        .param("username", "${username}")
        .param("email", "${contactEmail}")
        .param("password", "${password}")
        .param("password_confirm", "${password}")
        .headers(headers_3)
        .check(status.is(200),
          jsonPath("$.success").is("true"))
}