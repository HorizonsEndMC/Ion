package net.starlegacy.spacegenerator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.starlegacy.spacegenerator.asteroid.AsteroidData;
import net.starlegacy.spacegenerator.asteroid.AsteroidOreDistribution;
import net.starlegacy.spacegenerator.asteroid.CachedAsteroid;
import net.starlegacy.util.Vec3i;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// java used here to optimize performance
public class AsteroidGenerator {
    private final static int RANDOM_ASTEROID_PADDING = 5;
    private final static int BELT_PADDING = 50;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SpaceGeneratorConfig.World worldConfig;
    private final AsteroidOreDistribution randomAsteroidDistribution;
    private final LoadingCache<SpaceGeneratorConfig.World.AsteroidBelt, AsteroidOreDistribution> beltDistributions;

    private static final int[][][] rotationMatrices = new int[][][]{
            new int[][]{
                    new int[]{+1, +0, +0},
                    new int[]{+0, +1, +0},
                    new int[]{+0, +0, +1}
            },
            new int[][]{
                    new int[]{+0, +0, -1},
                    new int[]{+0, +1, +0},
                    new int[]{+1, +0, +0}
            },
            new int[][]{
                    new int[]{+-1, +0, +0},
                    new int[]{+0, +1, +0},
                    new int[]{+0, +0, -1}
            },
            new int[][]{
                    new int[]{+0, +0, +1},
                    new int[]{+0, +1, +0},
                    new int[]{+1, +0, +0}
            },
            new int[][]{
                    new int[]{+1, +0, +0},
                    new int[]{+0, +0, -1},
                    new int[]{+0, +1, +0}
            },
            new int[][]{
                    new int[]{+1, +0, +0},
                    new int[]{+0, +0, +1},
                    new int[]{+0, -1, +0}
            }
    };

    public AsteroidGenerator(SpaceGeneratorConfig.World worldConfig) {
        this.worldConfig = worldConfig;
        randomAsteroidDistribution = new AsteroidOreDistribution(worldConfig.getRandomAsteroidOreDistribution());
        beltDistributions = CacheBuilder.newBuilder()
                .weakKeys()
                .build(CacheLoader.from(this::getBeltDistribution));
    }

    @NotNull
    private AsteroidOreDistribution getBeltDistribution(SpaceGeneratorConfig.World.AsteroidBelt belt) {
        if (belt == null) {
            return new AsteroidOreDistribution(new HashMap<>());
        }

        return new AsteroidOreDistribution(belt.getOreDistribution());
    }

    public void addAsteroids(World world, int x, int z, ChunkGenerator.ChunkData data) {
        Random random = new Random();

        long worldSeed = world.getSeed();

        random.setSeed(worldSeed);
        long xRand = ((random.nextLong() / 2L) * 2L) + 1L;
        long zRand = ((random.nextLong() / 2L) * 2L) + 1L;

        for (int neighborX = x - 8; neighborX <= x + 8; neighborX++) {
            for (int neighborZ = z - 8; neighborZ <= z + 8; neighborZ++) {
                long neighborSeed = (((long) neighborX * xRand) + ((long) neighborZ * zRand)) ^ worldSeed;

                random.setSeed(neighborSeed);
                checkBeltAsteroids(world, x, z, data, neighborX, neighborZ, random);
                checkRandomAsteroid(world, x, z, data, neighborX, neighborZ, random);
            }
        }
    }

    private void checkBeltAsteroids(World world,
                                    int x,
                                    int z,
                                    ChunkGenerator.ChunkData data,
                                    int neighborX,
                                    int neighborZ,
                                    Random random) {
        int blockX = neighborX << 4;
        int blockZ = neighborZ << 4;
        for (SpaceGeneratorConfig.World.AsteroidBelt belt : worldConfig.getAsteroidBelts()) {
            double distance = Math.sqrt(Math.pow(blockX - belt.getX(), 2.0) + Math.pow(blockZ - belt.getZ(), 2.0));
            double distanceFromRadius = Math.abs(belt.getRadius() - distance);

            if (distanceFromRadius >= belt.getThickness()) {
                continue;
            }

            checkBeltAsteroid(world, x, z, data, neighborX, neighborZ, random, belt);
        }
    }

    private void checkRandomAsteroid(World world,
                                     int chunkX,
                                     int chunkZ,
                                     ChunkGenerator.ChunkData data,
                                     int neighborChunkX,
                                     int neighborChunkZ,
                                     Random random) {
        float asteroidChance = random.nextFloat();
        if (asteroidChance > 1.0f / worldConfig.getRandomAsteroidSparsity()) {
            return;
        }

        // todo: pre-generate an array of cached asteroids to remove string lookup
        List<String> asteroids = worldConfig.getRandomAsteroids();

        tryPlaceAsteroidSection(world,
                chunkX,
                chunkZ,
                data,
                neighborChunkX,
                neighborChunkZ,
                random,
                asteroids,
                RANDOM_ASTEROID_PADDING,
                this.randomAsteroidDistribution);
    }

    private void checkBeltAsteroid(World world,
                                   int chunkX,
                                   int chunkZ,
                                   ChunkGenerator.ChunkData data,
                                   int neighborChunkX,
                                   int neighborChunkZ,
                                   Random random,
                                   SpaceGeneratorConfig.World.AsteroidBelt belt) {
        if (belt.getAsteroids().isEmpty()) {
            return;
        }

        float asteroidChance = random.nextFloat();
        if (asteroidChance > 1.0f / belt.getSparsity()) {
            return;
        }

        List<String> asteroids = belt.getAsteroids();
        AsteroidOreDistribution distribution = beltDistributions.getUnchecked(belt);

        tryPlaceAsteroidSection(world,
                chunkX,
                chunkZ,
                data,
                neighborChunkX,
                neighborChunkZ,
                random,
                asteroids,
                BELT_PADDING,
                distribution);
    }

    private void tryPlaceAsteroidSection(World world,
                                         int chunkX,
                                         int chunkZ,
                                         ChunkGenerator.ChunkData data,
                                         int neighborChunkX,
                                         int neighborChunkZ,
                                         Random random,
                                         List<String> asteroidNames,
                                         int padding,
                                         AsteroidOreDistribution distribution) {
        if (asteroidNames.isEmpty()) {
            return;
        }

        // todo: pre-generate an array of cached asteroids to remove string lookup
        int asteroidIndex = random.nextInt(asteroidNames.size());
        String asteroidName = asteroidNames.get(asteroidIndex);
        CachedAsteroid asteroid = AsteroidData.cachedAsteroids.get(asteroidName).getValue();

        int width = asteroid.getWidth();
        int height = asteroid.getHeight();
        int length = asteroid.getLength();

        int yBound = world.getMaxHeight() - height - (padding * 2);

        int asteroidX = random.nextInt(16) + (neighborChunkX << 4);
        int asteroidY = random.nextInt(yBound) + padding;
        int asteroidZ = random.nextInt(16) + (neighborChunkZ << 4);

        int rotationMatrixIndex = random.nextInt(rotationMatrices.length);
        int[][] matrix = rotationMatrices[rotationMatrixIndex];

        int[] row1 = matrix[0];
        int[] row2 = matrix[1];
        int[] row3 = matrix[2];

        // this assumes that all coordinates within cached asteroid are relative
        // to its min point, and if you add width/height/length, you get max point

        int farthestX = asteroidX + (width * row1[0]) + (height * row1[1]) + (length * row1[2]);
        int farthestY = asteroidY + (width * row2[0]) + (height * row2[1]) + (length * row2[2]);
        int farthestZ = asteroidZ + (width * row3[0]) + (height * row3[1]) + (length * row3[2]);

        int minX = Math.min(asteroidX, farthestX);
        int minY = Math.min(asteroidY, farthestY);
        int minZ = Math.min(asteroidZ, farthestZ);
        int maxX = Math.max(asteroidX, farthestX);
        int maxY = Math.max(asteroidY, farthestY);
        int maxZ = Math.max(asteroidZ, farthestZ);

        int maxHeight = world.getMaxHeight();

        if (minY < 0 || maxY >= maxHeight) {
            return;
        }

        int minChunkX = minX >> 4, minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4, maxChunkZ = maxZ >> 4;

        if (chunkX < minChunkX || chunkX > maxChunkX || chunkZ < minChunkZ || chunkZ > maxChunkZ) {
            return;
        }


        placeBlocks(chunkX, chunkZ, data, neighborChunkX, neighborChunkZ, random, asteroidName, asteroid, distribution, asteroidX, asteroidY, asteroidZ, rotationMatrixIndex, row1, row2, row3, maxHeight);
    }

    private void placeBlocks(int chunkX,
                             int chunkZ,
                             ChunkGenerator.ChunkData data,
                             int neighborChunkX,
                             int neighborChunkZ,
                             Random random,
                             String asteroidName,
                             CachedAsteroid asteroid,
                             AsteroidOreDistribution distribution,
                             int asteroidX,
                             int asteroidY,
                             int asteroidZ,
                             int rotationMatrixIndex,
                             int[] row1,
                             int[] row2,
                             int[] row3,
                             int maxHeight) {
        // TODO: Make this ordered by *something* so that the seed determines the ore locations, not map randomness
        for (Map.Entry<Vec3i, BlockData> entry : asteroid.getBlocks().entrySet()) {
            Vec3i offset = entry.getKey();
            BlockData blockData = entry.getValue();

            int dx = offset.getX(), dy = offset.getY(), dz = offset.getZ();

            int x = asteroidX + (dx * row1[0]) + (dy * row1[1]) + (dz * row1[2]);
            int y = asteroidY + (dx * row2[0]) + (dy * row2[1]) + (dz * row2[2]);
            int z = asteroidZ + (dx * row3[0]) + (dy * row3[1]) + (dz * row3[2]);

            // only paste the parts in the current chunk
            if (x >> 4 != chunkX || z >> 4 != chunkZ) {
                continue;
            }

            if (y < 0 || y >= maxHeight) {
                log.warn("Placing asteroid " + asteroidName
                        + " at " + asteroidX + ", " + asteroidY + "," + asteroidZ
                        + " rotation " + rotationMatrixIndex
                        + " with block at invalid Y level " + x + ", " + y + ", " + z
                        + " in chunk " + chunkX + ", " + chunkZ
                        + " from neighbor " + neighborChunkX + ", " + neighborChunkZ);
                continue;
            }

            Material material = blockData.getMaterial();
            if (AsteroidData.ORE_REPLACE_TYPES.contains(material)) {
                blockData = distribution.pickType(blockData, random);
            }

            data.setBlock(x & 15, y, z & 15, blockData);
        }
    }
}
