package loadtest.friends

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._

import scala.concurrent.duration._

import loadtest.util._
import loadtest.settings._


class AddFriendsScenario(val name: String = "add-friend",
												 val iterations: Int = 1)
	extends Headers
	with UserInfoFeeder
	with FacebookUserFeeder
{
  val scn = scenario(name)
  	.repeat(iterations) {
	    feed(user_info_generator)
	    .feed(facebook_user_generator)

	    .exec(RestAPI.add_friend)
	    .pause(40 milliseconds, 120 milliseconds)
  	}
}