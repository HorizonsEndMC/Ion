package net.horizonsend.ion.server.features.multiblock.newcrafting.input

class BuildableRecipeInput {


	companion object {
		fun builder(): Builder = Builder()
	}

	class Builder() {
		fun build(): BuildableRecipeInput {
			return BuildableRecipeInput()
		}
	}
}
