package net.horizonsend.ion.server.features.world.data

interface DataFixer {
	/**
	 * The data version this will apply to.
	 * If the target data's version is lower than this value, this will be triggered
	 **/
	val dataVersion: Int
}
