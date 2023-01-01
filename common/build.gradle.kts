plugins { `java-library` }

dependencies {
	// Library Loaded
	compileOnlyApi(libs.bundles.configurate)
	compileOnlyApi(libs.bundles.kotlin)
	compileOnlyApi(libs.jedis)
	compileOnlyApi(libs.kmongo)
}
