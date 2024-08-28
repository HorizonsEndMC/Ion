package net.horizonsend.ion.server.features.multiblock.newer

import com.google.common.collect.Multimap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.features.multiblock.type.misc.TestMultiblock
import net.horizonsend.ion.server.features.multiblock.type.powerbank.new.NewPowerBankMultiblockTier1
import net.horizonsend.ion.server.features.multiblock.type.powerbank.new.NewPowerBankMultiblockTier2
import net.horizonsend.ion.server.features.multiblock.type.powerbank.new.NewPowerBankMultiblockTier3
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf

//TODO store the progress of migrated multiblocks here
object MultiblockRegistration : IonServerComponent() {
	private val multiblocks: MutableMap<String, Multiblock> = mutableMapOf()
	private val multiblockCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
	/**
	 * The multiblocks grouped by their sign text.
	 *
	 * E.g. powerbank would contain all tiers of Power Banks
	 **/
	private val byDetectionName : Multimap<String, Multiblock> = multimapOf()

	override fun onEnable() {
		initMultiblocks()
		sortMultiblocks()

		log.info("Loaded ${multiblocks.size} multiblocks")
	}

	private fun initMultiblocks() {
        registerMultiblock(TestMultiblock)
		registerMultiblock(NewPowerBankMultiblockTier1)
		registerMultiblock(NewPowerBankMultiblockTier2)
		registerMultiblock(NewPowerBankMultiblockTier3)
		registerMultiblock(MobDefender)
	}

	private fun sortMultiblocks() {
		for ((_, multi) in getAllMultiblocks()) {
			byDetectionName[multi.name].add(multi)
		}
	}

	private fun registerMultiblock(multiblock: Multiblock) {
		val name = multiblock.javaClass.simpleName ?: throw IllegalArgumentException("Provided anonymous multiblock class!")

		if (multiblocks.containsKey(name)) {
			throw IllegalArgumentException("Attempted to register duplicate multiblock name! Exisitng: ${multiblocks[name]}, new: $multiblock")
		}

		multiblocks[name] = multiblock
	}

	fun getAllMultiblocks() = multiblocks

	fun getBySignName(name: String): List<Multiblock> {
		return byDetectionName[name].toList()
	}

	fun getByStorageName(name: String): Multiblock? {
		return multiblocks[name]
	}
}
