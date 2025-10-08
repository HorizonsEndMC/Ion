package net.horizonsend.ion.server.miscellaneous

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.registry.event.RegistryEvents
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.InterceptorCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.LaserCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.PulseCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.TestBoidCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.CapitalBeamStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.CthulhuBeamStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.FlamethrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.GazeStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.MiniPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.PumpkinCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.SkullThrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.SonicMissileWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.DoomsdayDeviceWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.HeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.HorizontalRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.TopArsenalStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.TorpedoStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.misc.PointDefenseStarshipWeaponMultiblockTop
import net.minecraft.core.cauldron.CauldronInteraction
import net.minecraft.core.component.DataComponents
import net.minecraft.stats.Stats
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.LayeredCauldronBlock
import org.bukkit.damage.DamageEffect
import org.bukkit.damage.DamageScaling
import org.bukkit.damage.DeathMessageType
import org.bukkit.event.block.CauldronLevelChangeEvent
import org.bukkit.plugin.java.JavaPlugin

@Suppress("Unused", "UnstableApiUsage")
class IonBootstrapper : PluginBootstrap {
	override fun bootstrap(context: BootstrapContext) {
		val damageMultiblocks = setOf(
			InterceptorCannonStarshipWeaponMultiblock,
			LaserCannonStarshipWeaponMultiblock,
			PlasmaCannonStarshipWeaponMultiblock,
			PulseCannonStarshipWeaponMultiblock,
			CapitalBeamStarshipWeaponMultiblock,
			CthulhuBeamStarshipWeaponMultiblockTop,
			FlamethrowerStarshipWeaponMultiblock,
			GazeStarshipWeaponMultiblock,
			MiniPhaserStarshipWeaponMultiblock,
			PumpkinCannonStarshipWeaponMultiblock,
			SkullThrowerStarshipWeaponMultiblock,
			SonicMissileWeaponMultiblock,
			TopArsenalStarshipWeaponMultiblock,
			DoomsdayDeviceWeaponMultiblock,
			HeavyLaserStarshipWeaponMultiblock,
			PhaserStarshipWeaponMultiblock,
			HorizontalRocketStarshipWeaponMultiblock,
			TorpedoStarshipWeaponMultiblock,
			PointDefenseStarshipWeaponMultiblockTop,
			TestBoidCannonStarshipWeaponMultiblock,
		)



		context.lifecycleManager.registerEventHandler(RegistryEvents.DAMAGE_TYPE.freeze().newHandler { event ->
			for (weapon in damageMultiblocks) {
				event.registry().register(
					weapon.damageTypeKey,
				) { builder ->
					builder
						.deathMessageType(DeathMessageType.DEFAULT)
						.messageId(weapon.key)
						.damageScaling(DamageScaling.NEVER)
						.damageEffect(DamageEffect.HURT)
						.exhaustion(1.5f)
				}
			}
		})

		fun addCauldronInteraction(itemType: Item) {
			CauldronInteraction.WATER.map[itemType] = CauldronInteraction { blockState, level, blockPos, player, interactionHand, itemStack, direction ->
				if (!itemStack.`is`(ItemTags.DYEABLE)) {
					return@CauldronInteraction InteractionResult.TRY_WITH_EMPTY_HAND
				} else if (!itemStack.has(DataComponents.DYED_COLOR)) {
					return@CauldronInteraction InteractionResult.TRY_WITH_EMPTY_HAND
				} else {
					if (!level.isClientSide) {
						if (!LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos, player, CauldronLevelChangeEvent.ChangeReason.ARMOR_WASH)) {
							return@CauldronInteraction InteractionResult.SUCCESS
						}

						itemStack.remove(DataComponents.DYED_COLOR)
						player.awardStat(Stats.CLEAN_ARMOR)
					}

					return@CauldronInteraction InteractionResult.SUCCESS
				}
			}
		}

		addCauldronInteraction(Items.WARPED_FUNGUS_ON_A_STICK)
		addCauldronInteraction(Items.DIAMOND_PICKAXE)
	}
	override fun createPlugin(context: PluginProviderContext): JavaPlugin = IonServer
}
