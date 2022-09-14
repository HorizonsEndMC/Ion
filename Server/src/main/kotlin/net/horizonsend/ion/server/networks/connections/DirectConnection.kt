package net.horizonsend.ion.server.networks.connections

class DirectConnection : AbstractConnection() {
	override fun checkIsValid(): Boolean = super.checkIsValid() && directContact(a, b)

	companion object : AbstractConnectionCompanion<DirectConnection>() {
		override fun construct(): DirectConnection = DirectConnection()
	}
}