package net.horizonsend.ion.server.features.multiblock.newer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.TestMultiblock
import net.horizonsend.ion.server.features.multiblock.type.powerbank.new.NewPowerBankMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.powerbank.new.NewPowerBankMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.powerbank.new.NewPowerBankMultiblockTier3

//TODO store the progress of migrated multiblocks here
object NewMultiblocks : IonServerComponent() {
	private val multiblocks: MutableMap<String, Multiblock> = mutableMapOf()
	private val multiblockCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	override fun onEnable() {
		initMultiblocks()

		log.info("Loaded ${multiblocks.size} multiblocks")
	}

	private fun initMultiblocks() {
        registerMultiblock(TestMultiblock)
		registerMultiblock(NewPowerBankMultiblockTier1)
		registerMultiblock(NewPowerBankMultiblockTier2)
		registerMultiblock(NewPowerBankMultiblockTier3)
	}

	private fun registerMultiblock(multiblock: Multiblock) {
		val name = multiblock.javaClass.simpleName ?: throw IllegalArgumentException("Provided anonymous multiblock class!")

		if (multiblocks.containsKey(name)) {
			throw IllegalArgumentException("Attempted to register duplicate multiblock name! Exisitng: ${multiblocks[name]}, new: $multiblock")
		}

		multiblocks[name] = multiblock
	}

	fun setMultiblock(x: Int, y: Int, z: Int) {

	}


}
