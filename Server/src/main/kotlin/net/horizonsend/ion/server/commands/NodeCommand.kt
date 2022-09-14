package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Values
import net.horizonsend.ion.server.IonWorld
import net.horizonsend.ion.server.networks.connectionTypes
import net.horizonsend.ion.server.networks.nodeTypes
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.entity.Player

@CommandAlias("nodes")
class NodeCommand : BaseCommand() {
	@Suppress("Unused")
	@Subcommand("rebuild")
	fun onRebuildNodesCommand(sender: Player, @Default("0") @Values("@range:10") radius: Int) {
		val ionWorld = IonWorld[(sender.world as CraftWorld).handle]

		val centreChunkX = sender.chunk.x
		val centreChunkZ = sender.chunk.z

		val startTime = System.currentTimeMillis()
		var chunks = 0
		var nodes = 0

		for (chunkX in centreChunkX - radius .. centreChunkX + radius)
		for (chunkZ in centreChunkZ - radius .. centreChunkZ + radius) {
			val aIonChunk = ionWorld[ChunkPos.asLong(chunkX, chunkZ)]
			aIonChunk.clear()

			chunks++

			val aLevelChunk = aIonChunk.levelChunk ?: continue

			for (aSection in aLevelChunk.sections) {
				val aPalettedContainer = aSection.states

				for (nodeType in nodeTypes) {
					if (!aPalettedContainer.maybeHas { it.block == nodeType.nodeBlock }) continue

					aPalettedContainer.forEachLocation { blockState, index ->
						if (blockState.block != nodeType.nodeBlock) return@forEachLocation

						val globalX = (aLevelChunk.locX shl 4) + index and 15
						val globalY = aSection.bottomBlockY() + index ushr 4
						val globalZ = (aLevelChunk.locZ shl 4) + index ushr 8

						val a = nodeType.build(aIonChunk, BlockPos(globalX, globalY, globalZ))

						for ((_, bIonChunk) in ionWorld.chunkKeyToIonChunk) {
							// Skip chunks that are not aligned and as a result cannot form connections.
							if (aIonChunk.chunkX != bIonChunk.chunkX && aIonChunk.chunkZ != bIonChunk.chunkZ) continue

							bIonChunk.iterateNodes { b ->
								for (connectionType in connectionTypes) {
									val connection = connectionType.buildValidated(a, b) ?: continue
									a.connections.add(connection)
									b.connections.add(connection)
								}
							}
						}

						aIonChunk.addNode(a)
						nodes++

						return@forEachLocation
					}
				}
			}
		}

		val delta = System.currentTimeMillis() - startTime

		sender.sendMessage(text("$nodes nodes in $chunks chunks were rebuilt in ${delta}ms.", color(0x7fff7f)))
	}

	@Subcommand("save")
	@Suppress("Unused")
	@CommandPermission("ion.networks.save")
	fun onSaveCommand(sender: Player) {
		val ionWorld = IonWorld[(sender.world as CraftWorld).handle]
		ionWorld.save()
		sender.sendMessage(text("Saved nodes for ${ionWorld.serverLevel.dimension().location()}!", color(0x7fff7f)))
	}

	@Subcommand("save")
	@Suppress("Unused")
	@CommandCompletion("@worlds")
	@CommandPermission("ion.networks.save")
	fun onSaveCommand(sender: CommandSender, world: String) {
		val bukkitWorld = Bukkit.getWorld(world)

		if (bukkitWorld == null) {
			sender.sendMessage(text("World $world does not exist!", color(0xff7f7f)))
			return
		}

		val ionWorld = IonWorld[(bukkitWorld as CraftWorld).handle]
		ionWorld.save()
		sender.sendMessage(text("Saved nodes for ${ionWorld.serverLevel.dimension().location()}!", color(0x7fff7f)))
	}

	@Suppress("Unused")
	@Subcommand("save all")
	@CommandPermission("ion.networks.save")
	fun onSaveAllCommand(sender: CommandSender) {
		IonWorld.saveAll()
		sender.sendMessage(text("Saved nodes for all worlds!", color(0x7fff7f)))
	}
}