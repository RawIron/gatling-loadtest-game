package loadtest.friends

import io.gatling.core.Predef._

import loadtest.friends.settings._


trait FacebookUserFeeder
{
  import scala.util.parsing.json._
  import scala.math._
  import scala.util.Random
  private val RNG = new Random

  val MAX_BUCKET = 100
  val MAX_PER_BUCKET = 100000
  val MAX_FRIENDS = Settings.MAX_FRIENDS

  val friends_buckets = new Array[Array[String]](MAX_BUCKET)
  var friends = new Array[String](0)
  var friends_position: Int = 0
  var buckets_position: Int = 0

  def pick_bucket = {
    if (buckets_position > 0) { RNG.nextInt(buckets_position) }
    else 0
  }
  def pick_position = {
    if (friends_position > 0) { RNG.nextInt(friends_position) }
    else 0
  }
  def pick_friend = friends_buckets(pick_bucket)(pick_position)

  def pick_friendcount = {
    if (friends_position > 0) { RNG.nextInt(min(MAX_FRIENDS, friends_position)) }
    else 0
  }

  def open_bucket = {
    friends_buckets(buckets_position) = new Array[String](MAX_PER_BUCKET)
    friends = friends_buckets(buckets_position)
    friends_position = 0
  }

  def new_friend(user: String) = {
    friends(friends_position) = user
    friends_position += 1
  }


  val friend_list_generator = new Feeder[String] {
    // always return true as this feeder can be polled infinitively
    override def hasNext = true

    override def next: Map[String, String] = {
      val friendcount = pick_friendcount
      val friendList = new Array[String](friendcount)
      for (i <- 0 to friendcount-1) {
        friendList(i) = pick_friend
      }
      val jsonFriendsList = JSONArray(friendList.toList).toString()
      Map("my_friends" -> jsonFriendsList)
      }
  }

  val facebook_user_generator = new Feeder[String] {
    // always return true as this feeder can be polled infinitively
    override def hasNext = true

    override def next: Map[String, String] = {
      val user = scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits).toString
      val name = user + "_name"

      if (friends_position == 0) {
        open_bucket
      }
      if (friends_position >= MAX_PER_BUCKET) {
        buckets_position += 1
        open_bucket
      }
      new_friend(user)

      Map("facebook_user" -> user,
          "facebook_name" -> name)
      }
  }
}