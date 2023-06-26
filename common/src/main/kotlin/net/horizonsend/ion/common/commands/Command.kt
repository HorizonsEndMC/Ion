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

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder

interface Command<S> {

    /**
     * Builds a [literal][LiteralArgumentBuilder] argument that can be used by
     * the [register][CommandDispatcher.register] function on a dispatcher to
     * register this command.
     */
    fun buildLiteral(): LiteralArgumentBuilder<S>
    fun buildLiterals(): List<LiteralArgumentBuilder<S>>
}

/**
 * [Builds][Command.buildLiteral] a [literal][LiteralArgumentBuilder] argument
 * from the specific [command] and registers it to this dispatcher.
 */
fun <S> CommandDispatcher<S>.register(command: Command<S>) {
	command.buildLiterals().forEach { register(it) }
}
