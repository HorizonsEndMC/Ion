package net.starlegacy.spacegenerator;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

// java used here to optimize performance
public class SpaceChunkGenerator extends ChunkGenerator {
    private final AsteroidGenerator asteroidGenerator;

    public SpaceChunkGenerator(SpaceGeneratorConfig.World worldConfig) {
        asteroidGenerator = new AsteroidGenerator(worldConfig);
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public @NotNull ChunkData generateChunkData(@NotNull World world,
                                                @NotNull Random random,
                                                int x,
                                                int z,
                                                @NotNull BiomeGrid biomeGrid) {
        setBiome(world, biomeGrid);

        ChunkData chunkData = createChunkData(world);
        asteroidGenerator.addAsteroids(world, x, z, chunkData);
        return chunkData;
    }

    private void setBiome(World world, BiomeGrid biomeGrid) {
        Biome biome;

        switch (world.getEnvironment()) {
            case NORMAL:
                biome = Biome.THE_VOID;
                break;
            case NETHER:
                biome = Biome.SOUL_SAND_VALLEY;
                break;
            case THE_END:
                biome = Biome.THE_END;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + world.getEnvironment());
        }

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 16; z++) {
                    biomeGrid.setBiome(x, y, z, biome);
                }
            }
        }
    }
}
