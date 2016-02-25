package loadtest.scoreboards

import io.gatling.core.Predef._


trait ScoreFeeder
{
  val score_generator = new Feeder[String] {
    import scala.util.Random

    private val RNG = new Random
    private def randInt(a:Int, b:Int) = RNG.nextInt(b-a) + a

    override def hasNext = true

    override def next: Map[String,String] = {
      val score = randInt(1, 1000)
      Map("score" -> score.toString)
    }
  }
}