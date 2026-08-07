package net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object HyperHyperDrive : HyperdriveMultiblock() {
	override val name="hyperdrive"
	override val maxPower: Int = 100_000
	override val hyperdriveClass: Int = 4
	override val displayName: Component get() = text("Hyper Hyperdrive")
	override val signText: Array<Component?> = createSignText(
		line1 = "&7Class",
		line2 = "&dHyper",
		line3 = "&bHyperdrive",
		line4 = null,
	)
	override val chetheritePerInventory: Int = 12

	override fun buildFuelInventoryOffsets() = listOf(
		Vec3i(x = 0, y = 1, z = 0),
		Vec3i(x = 0, y = 1, z = 0),
		Vec3i(x = 0, y = 1, z = 0),
		Vec3i(x = 0, y = 1, z = 0),
		Vec3i(x = 0, y = 1, z = 0),
		Vec3i(x = 0, y = 1, z = 0)
	)

	override fun addFuelInventories(multiblockShape: MultiblockShape) {
		buildFuelInventoryOffsets().forEach { (x, y, z) ->multiblockShape.at(x, y, z).anyPipedInventory() }
	}

	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(0) {
				x(2).anyStairs()
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
				x(-2).anyStairs()
			}
			y(1) {
				x(2).ironBlock()
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
				x(-2).ironBlock()
			}
			y(2) {
				x(2).anyStairs()
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
				x(-2).anyStairs()
			}
		}
		z(1) {
			y(0) {
				x(2).anyGlass()
				x(1).emeraldBlock()
				x(0).anyGlass()
				x(-1).emeraldBlock()
				x(-2).anyGlass()
			}
			y(1) {
				x(2).anyGlass()
				x(1).emeraldBlock()
				x(0).anyGlass()
				x(-1).emeraldBlock()
				x(-2).anyGlass()
			}
			y(2) {
				x(2).anyGlass()
				x(1).emeraldBlock()
				x(0).anyGlass()
				x(-1).emeraldBlock()
				x(-2).anyGlass()
			}
		}
		z(0) {
			y(0) {
				x(2).anyStairs()
				x(1).ironBlock()
				x(0).powerInput()
				x(-1).ironBlock()
				x(-2).anyStairs()
			}
			y(1) {
				x(2).ironBlock()
				x(1).ironBlock()
				x(0).anyPipedInventory()
				x(-1).ironBlock()
				x(-2).ironBlock()
			}
			y(2) {
				x(2).anyStairs()
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
				x(-2).anyStairs()
			}
		}
	}}
