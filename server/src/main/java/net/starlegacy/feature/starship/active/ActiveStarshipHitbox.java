package net.starlegacy.feature.starship.active;

import net.horizonsend.ion.server.miscellaneous.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.starlegacy.util.CoordinatesKt.*;

public class ActiveStarshipHitbox {
    /**
     * First dimension: array of z axes
     * Second dimension: Array of y axis bounds
     * Third dimension: arrays with length 2, 0 is min y, 1 is max y
     */
    @NotNull
    private int[][][] boundsArray = new int[0][][];
    @NotNull
    private Vec3i min = new Vec3i(0, 0, 0);
    @NotNull
    private Vec3i max = new Vec3i(0, 0, 0);

    public ActiveStarshipHitbox(Set<Long> blocks) {
        calculate(blocks);
    }

    public @NotNull Vec3i getMin() {
        return min;
    }

    public @NotNull Vec3i getMax() {
        return max;
    }

    public void calculate(@NotNull Set<Long> blocks) {
        calculateMinMax(blocks);
        calculateBounds(blocks);
    }

    public void calculateBounds(@NotNull Set<Long> blocks) {
        if (blocks.isEmpty()) {
            min = new Vec3i(0, 0, 0);
            max = new Vec3i(0, 0, 0);
            return;
        }

        int minX = min.getX();
        int minY = min.getY();
        int minZ = min.getZ();
        int width = max.getX() - minX + 1;

        int length = max.getZ() - minZ + 1;
        boundsArray = new int[width][][];

        for (long key : blocks) {
            int x = blockKeyX(key) - minX;
            int y = blockKeyY(key) - minY;
            int z = blockKeyZ(key) - minZ;

            int[][] yBoundsArray = boundsArray[x];

            if (yBoundsArray == null) {
                yBoundsArray = new int[length][];
                boundsArray[x] = yBoundsArray;
            }

            int[] yBounds = yBoundsArray[z];

            if (yBounds == null) {
                yBounds = new int[2];
                yBounds[0] = y;
                yBounds[1] = y;
                boundsArray[x][z] = yBounds;
            } else if (y < yBounds[0]) {
                yBounds[0] = y;
            } else if (y > yBounds[1]) {
                yBounds[1] = y;
            }
        }
    }

    public void calculateMinMax(Set<Long> blocks) {
        if (blocks.isEmpty()) {
            boundsArray = new int[0][][];
            return;
        }

        long start = blocks.iterator().next();
        int minX = blockKeyX(start), minY = blockKeyY(start), minZ = blockKeyZ(start);
        int maxX = minX, maxY = minY, maxZ = minZ;

        for (long key : blocks) {
            int x = blockKeyX(key);
            int y = blockKeyY(key);
            int z = blockKeyZ(key);

            if (x < minX) minX = x;
            if (x > maxX) maxX = x;

            if (y < minY) minY = y;
            if (y > maxY) maxY = y;

            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        min = new Vec3i(minX, minY, minZ);
        max = new Vec3i(maxX, maxY, maxZ);
    }

    public boolean contains(int x, int y, int z, int minX, int minY, int minZ, int tolerance) {
        int[][] zArray = boundsArray[x - minX];
        if (zArray == null) {
            return false;
        }

        int[] yBounds = zArray[z - minZ];
        if (yBounds == null) {
            return false;
        }

        int yDiff = y - minY;
        return yDiff >= yBounds[0] - tolerance && yDiff <= yBounds[1] + tolerance;
    }
}
