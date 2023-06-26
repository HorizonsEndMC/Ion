/*
 * Copyright 2020-present Nicolai Christophersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.horizonsend.ion.common.commands

import com.mojang.brigadier.context.CommandContext

/**
 * The context for the execution of commands.
 *
 * @see ExecutableCommand
 */
interface ExecutionContext<S> {

    /**
     * The Brigadier command context for this command.
     */
    val context: CommandContext<S>

    /**
     * The source that invoked the command being executed.
     */
    val source: S
}

/**
 * A function type with [ExecutableCommand] as its receiver, such that its
 * properties are available within the scope of the function body.
 */
typealias ExecutableCommand<S> = ExecutionContext<S>.() -> Unit
