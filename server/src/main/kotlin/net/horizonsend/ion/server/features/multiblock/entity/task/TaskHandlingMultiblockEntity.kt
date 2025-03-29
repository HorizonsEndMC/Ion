package net.horizonsend.ion.server.features.multiblock.entity.task

interface TaskHandlingMultiblockEntity<T: MultiblockEntityTask<*>> {
	var task: T?

	fun startTask(task: T) {
		this.task = task
		task.onEnable()
	}

	fun stopTask() {
		task?.onDisable()
		task = null
	}
}
