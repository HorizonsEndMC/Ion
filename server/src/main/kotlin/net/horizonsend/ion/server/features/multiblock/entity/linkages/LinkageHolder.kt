package net.horizonsend.ion.server.features.multiblock.entity.linkages

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement

sealed interface LinkageHolder {
	fun contains(holder: MultiblockEntity): Boolean
	fun getOwners(): Set<MultiblockEntity>
	fun getLinkages(): Set<MultiblockLinkage>

	fun displace(movement: StarshipMovement)
}

data class SingleMultiblockLinkage(val linkage: MultiblockLinkage) : LinkageHolder {
	override fun contains(holder: MultiblockEntity): Boolean {
		return this.linkage.owner == holder
	}

	override fun getOwners(): Set<MultiblockEntity> = setOf(linkage.owner)
	override fun getLinkages(): Set<MultiblockLinkage> = setOf(linkage)

	override fun displace(movement: StarshipMovement) = linkage.displace(movement)
}

class SharedMultiblockLinkage : LinkageHolder {
	private val linkages: ObjectOpenHashSet<MultiblockLinkage> = ObjectOpenHashSet()

	override fun contains(holder: MultiblockEntity): Boolean {
		return linkages.any { it.owner == holder }
	}

	override fun getOwners(): Set<MultiblockEntity> {
		return linkages.mapTo(mutableSetOf()) { it.owner }
	}

	override fun getLinkages(): Set<MultiblockLinkage> {
		return linkages
	}

	fun add(multiblockEntity: MultiblockLinkage) {
		linkages.add(multiblockEntity)
	}

	fun remove(multiblockEntity: MultiblockLinkage) {
		linkages.remove(multiblockEntity)
	}

	fun getAllHolders() = linkages.clone()

	override fun displace(movement: StarshipMovement) {
		return linkages.forEach { it.displace(movement) }
	}

	companion object {
		fun of(vararg entities: MultiblockLinkage): SharedMultiblockLinkage {
			val new = SharedMultiblockLinkage()
			entities.forEach(new::add)
			return new
		}
	}
}
