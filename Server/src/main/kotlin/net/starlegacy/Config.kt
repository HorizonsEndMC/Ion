package net.starlegacy

//import com.github.dahaka934.jhocon.annotations.Comment
//import com.github.dahaka934.jhocon.annotations.ValidatorRange

data class Config(
	//@Comment("Path to folder to keep configuration files shared between servers in")
	val sharedFolder: String = "shared/starlegacy",

	//@Comment("Whether this is the master server which runs certain tasks like settlement purges")
	val master: Boolean = false,

	//@Comment("Whether to allow players in the same group to attack eachother on this server")
	val allowFriendlyFire: Boolean = false,

	//@Comment("The multiplier for the cost of territories in credits for nations and settlements")
	val territoryCost: Int = 10,

	//@Comment("Connection details for the MongoDB database")
	val mongo: Mongo = Mongo(),

	val redis: Redis = Redis(),

	//@Comment("Settings for chat channels")
	val chat: Chat = Chat()
) {
	data class Mongo(
		//@Comment("The host IP address of the MongoDB database")
		val host: String = "mongo",

		//@Comment("The host port of the MongoDB database")
		val port: Int = 27017,

		//@Comment("The database to connect to")
		val database: String = "test",

		//@Comment("The username to use")
		val username: String = "test",

		//@Comment("The password to use")
		val password: String = "test"
	)

	data class Redis(
		val host: String = "redis",

		val channel: String = "starlegacytest"
	)

	data class Chat(
		//@Comment("The maximum amount of blocks away someone can be and still see someone's local chat")
		val localDistance: Int = 200,

		//@Comment("A list of names of worlds in which global chat should be disabled")
		val noGlobalWorlds: List<String> = listOf()
	)
}