package net.horizonsend.ion.server.features.transport.old.pipe.filter

import org.bukkit.Material

data class FilterItemData(val material: Material, val customItemIdentifier: String?) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as FilterItemData

		if (material != other.material) return false
		if (customItemIdentifier != other.customItemIdentifier) return false

		return true
	}

	override fun hashCode(): Int {
		var result = material.hashCode()
		result = 31 * result + (customItemIdentifier?.hashCode() ?: 0)
		return result
	}
}
