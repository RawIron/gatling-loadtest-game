package loadtest.settings


object LoadbalancerSettings
{
	val BASE_URL = "http://<YOUR_ELB_ADDRESS>.elb.amazonaws.com:8098"
}

object LoadSettings
{
  val ENDURANCE_TIME = 300
  val ENDURANCE_USERS: Double = 100

  val RAMP_TIME = 120
  val RAMP_USERS_START: Double = 10
  val RAMP_USERS_END: Double = 100

  val BURST_TIME = 120
  val BURST_USERS: Int = 200
}