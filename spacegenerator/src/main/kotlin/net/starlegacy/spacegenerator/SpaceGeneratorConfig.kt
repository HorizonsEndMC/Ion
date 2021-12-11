package net.starlegacy.spacegenerator

data class SpaceGeneratorConfig(val worlds: Map<String, World> = mapOf()) {
    data class World(
        val randomAsteroidSparsity: Double = 400.0,
        val randomAsteroidOreDistribution: Map<String, String> = mapOf(),
        val randomAsteroids: List<String> = listOf(),
        val asteroidBelts: List<AsteroidBelt> = listOf()
    ) {
        data class AsteroidBelt(
            val description: String,
            val x: Int,
            val z: Int,
            val radius: Int = 5250,
            val thickness: Int = 100,
            val sparsity: Double = 10.0,
            val oreDistribution: Map<String, String>,
            val asteroids: List<String>
        )
    }
}
