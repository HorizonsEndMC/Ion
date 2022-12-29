package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.ConditionFailedException
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import java.util.Random
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.generation.configuration.AsteroidConfiguration
import net.horizonsend.ion.server.generation.populators.AsteroidPopulator
import net.horizonsend.ion.server.generation.populators.OrePopulator
import net.horizonsend.ion.server.generation.populators.OrePopulator.Companion.getSphereBlocks
import net.minecraft.world.level.ChunkPos
import org.bukkit.World
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.craftbukkit.v1_19_R2.generator.CraftLimitedRegion
import org.bukkit.entity.Player

@CommandAlias("asteroid")
class AsteroidCommand : BaseCommand() {
    private val configuration: AsteroidConfiguration =
        loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

    @Suppress("unused")
    @CommandPermission("spacegenerator.regenerate")
    @Subcommand("regenerate asteroid")
    @CommandCompletion("Optional:Range")
    fun onRegenerateRangeAsteroid(sender: Player, range: Int = 0) {
        var placed = 0

        for (x in sender.chunk.x - range..sender.chunk.x + range) {
            for (z in sender.chunk.z - range..sender.chunk.z + range) {
                try { postGenerateAsteroids(sender.world, ChunkPos(x, z)) }
                catch(error: ConditionFailedException) {
                    sender.sendRichMessage("<red>${error.message}"); continue
                }

                placed += 1
            }
        }

        sender.sendRichMessage("<#7fff7f>Regenerated ores in $placed chunks!")
    }

    @Suppress("unused")
    @CommandPermission("spacegenerator.regenerate")
    @Subcommand("create ore")
    @CommandCompletion("Optional:Range")
    fun onCreateOres(sender: Player, range: Int) {
        val world = sender.world
        val craftWorld = (world as CraftWorld).handle
        val populator: OrePopulator =
            craftWorld.generator.getDefaultPopulators(world)
                .find { it is OrePopulator } as? OrePopulator ?: return

        val oreRandom = Random(System.currentTimeMillis() + world.seed)

        var chunkCount = 0

        for (x in sender.chunk.x - range..sender.chunk.x + range) {
            for (z in sender.chunk.z - range..sender.chunk.z + range) {
                val chunkPos = ChunkPos(x, z)
                val limitedRegion = CraftLimitedRegion(craftWorld, chunkPos)

                populator.apply {
                    this.populate(
                        sender.world,
                        oreRandom,
                        x,
                        z,
                        limitedRegion
                    )
                }

                chunkCount += 1
            }
        }

        sender.sendRichMessage("<#7fff7f>Success! Populated $chunkCount chunks with new ores!")
    }

    @Suppress("unused")
    @CommandPermission("spacegenerator.regenerate")
    @Subcommand("regenerate ore")
    @CommandCompletion("Optional:Range")
    fun onRegenerateRangeOres(sender: Player, range: Int = 0) {
        var placed = 0

        for (x in sender.chunk.x - range..sender.chunk.x + range) {
            for (z in sender.chunk.z - range..sender.chunk.z + range) {
                try { postGenerateOres(sender.world, ChunkPos(x, z)) }
                catch(error: ConditionFailedException) {
                    sender.sendRichMessage("<red>${error.message}"); continue
                }

                placed += 1
            }
        }

        sender.sendRichMessage("<#7fff7f>Regenerated ores in $placed chunks!")
    }

    @Suppress("unused")
    @CommandPermission("spacegenerator.regenerate")
    @Subcommand("create custom")
    @CommandCompletion("size index octaves")
    fun onCreateCustom(sender: Player, size: Double, index: Int, octaves: Int) {
        if (!IntRange(0, configuration.blockPalettes.size).contains(index)) {
            sender.sendRichMessage("<red>ERROR: index out of range: 0..${configuration.blockPalettes.size - 1}")
            return
        }

        val chunkPos = ChunkPos(sender.chunk.x, sender.chunk.z)
        val world = sender.world

        val craftWorld = (world as CraftWorld).handle

        val populator: AsteroidPopulator =
            craftWorld.generator.getDefaultPopulators(world)
                .find { it is AsteroidPopulator } as? AsteroidPopulator ?: return

        val asteroid = AsteroidPopulator.Asteroid(
            sender.location.x.toInt(),
			sender.location.y.toInt(),
			sender.location.z.toInt(),
            configuration.blockPalettes[index],
            size,
            octaves
        )

        populator.apply {
            this.postGenerateAsteroid(
                world,
                chunkPos.x,
                chunkPos.z,
                asteroid
            )
        }

        sender.sendRichMessage("<#7fff7f>Success!")
    }

    @Suppress("unused")
    @Subcommand("create random")
    fun onCreateRandom(sender: Player) {
        val chunkPos = ChunkPos(sender.chunk.x, sender.chunk.z)
        val world = sender.world

        val asteroidRandom = Random(chunkPos.x + chunkPos.z + world.seed)

        val craftWorld = (world as CraftWorld).handle

        val populator: AsteroidPopulator =
            craftWorld.generator.getDefaultPopulators(world)
                .find { it is AsteroidPopulator } as? AsteroidPopulator ?: return

        val asteroid = with(populator) {
            this.generateAsteroid(
				sender.location.x.toInt(),
				sender.location.y.toInt(),
				sender.location.z.toInt(),
                asteroidRandom
            )
        }

        val limitedRegion = CraftLimitedRegion(craftWorld, chunkPos)

        populator.apply {
            this.placeAsteroid(
                craftWorld.seed,
                chunkPos.x,
                chunkPos.z,
                limitedRegion,
                asteroid
            )
        }

        sender.sendRichMessage("<#7fff7f>Success!")
    }

    private fun postGenerateAsteroids(world: World, chunkPos: ChunkPos) {
        val asteroids = getChunkAsteroids(chunkPos.x, chunkPos.z)

        if (asteroids.isEmpty()) {
            throw ConditionFailedException("No asteroids to regenerate for Chunk (${chunkPos.x}, ${chunkPos.z})!")
        }

        val craftWorld = (world as CraftWorld).handle

        val populator: AsteroidPopulator =
            craftWorld.generator.getDefaultPopulators(world)
                .find { it is AsteroidPopulator } as? AsteroidPopulator ?: return

        val limitedRegion = CraftLimitedRegion(craftWorld, chunkPos)

        for (asteroid in asteroids) {
            populator.apply {
                this.placeAsteroid(
                    craftWorld.seed,
                    chunkPos.x,
                    chunkPos.z,
                    limitedRegion,
                    asteroid
                )
            }
        }
    }

    private fun postGenerateOres(world: World, chunkPos: ChunkPos) {
        val oreBlobs = getChunkOres(chunkPos.x, chunkPos.z)

        if (oreBlobs.isEmpty()) {
            throw ConditionFailedException("No ores to regenerate for Chunk (${chunkPos.x}, ${chunkPos.z})!")
        }

        for (ore in oreBlobs) {
            val oreBlocks = getSphereBlocks(ore.blobSize, origin = Triple(ore.centerX, ore.centerY, ore.centerZ))

            for (block in oreBlocks) {
                ore.material.let { world.setBlockData(block.first, block.second, block.third, it) }
            }
        }
    }

    private fun getChunkAsteroids(chunkX: Int, chunkZ: Int): List<AsteroidPopulator.Asteroid> {
        return listOf()
    }

    private fun getChunkOres(chunkX: Int, chunkZ: Int): List<OrePopulator.PlacedOre> {
        return listOf()
    }
}