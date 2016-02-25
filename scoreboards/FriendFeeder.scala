package loadtest.scoreboards

import io.gatling.core.Predef._
import scala.util.parsing.json._


trait FriendFeeder
{
  val friend_generator = new Feeder[String] {
    import scala.util.Random
    private val RNG = new Random
    private def randInt(a:Int, b:Int) = RNG.nextInt(b-a) + a

    override def hasNext = true

    override def next: Map[String, String] = {
      val friendcount = randInt(1, 50)
      val friendList = new Array[String](friendcount)
      for (i <- 0 to friendcount-1) {
        friendList(i) = randInt(1, 1000).toString()
      }
      val jsonFriendsList = JSONArray(friendList.toList).toString()
      Map("friends" -> jsonFriendsList)
      }
  }
}