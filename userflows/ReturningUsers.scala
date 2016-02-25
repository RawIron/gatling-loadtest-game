package loadtest.userflows


class User {
	val id: String = ""
	var returned = 0
}

/*
class ReturningUsersInBuckets {
	var user_buckets: Array[List(String)] //new Array[new List(new Map[String,String])](2)
	var bucket_index = 0
	val returning_user_bucket = 2
	val buckets = 64

	def put(user_info: Map[String,String]) = {
			user_buckets(bucket_index).add(user_info.username)
			bucket_index += 1
	}

	def pop: Map[String,String] = {
		user_buckets(returning_user_bucket).pop()
	}
}
*/

class ReturningUsersQueue {

}