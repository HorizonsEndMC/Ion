package net.horizonsend.ion.server.features.whereisit.mod

/**
 * States how an item was found, used for different colours.
 * NOT_FOUND: The item was not found.
 * FOUND: The item was found directly.
 * FOUND_DEEP: The item was found in a nested inventory (shulker box, backpack).
 */
enum class FoundType {
	NOT_FOUND, FOUND, FOUND_DEEP
}
