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

package net.horizonsend.ion.common.commands.dsl

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.horizonsend.ion.common.commands.*
import com.mojang.brigadier.Command as BrigadierCommand

sealed class DslCommandTree<S, A : ArgumentBuilder<S, A>>(
    private val contextRef: ContextRef<S>
) {

    protected var command: BrigadierCommand<S>? = null

    private val children = mutableListOf<DslCommandTree<S, out ArgumentBuilder<S, *>>>()

    private val subcommands = mutableListOf<Command<S>>()

    private var apply: (A.() -> Unit)? = null

    open fun executes(command: BrigadierCommand<S>) {
        check(this.command == null) { "Cannot reassign executes command" }
        this.command = command
    }

    fun runs(command: ExecutableCommand<S>) {
        executes { context ->
            val execution = object : ExecutionContext<S> {
                override val context = context
                override val source = context.source
            }

            command(execution)
            SINGLE_SUCCESS
        }
    }

    fun apply(apply: (A.() -> Unit)?) {
        this.apply = apply
    }

    private fun <N : DslCommandTree<S, A>, A : ArgumentBuilder<S, T>, T> addChildAndApply(
        node: N,
        apply: (A.() -> Unit)?
    ): N {
        return node.also(children::add).also { it.apply(apply) }
    }

    open fun literal(
        literal: String,
        apply: (LiteralArgumentBuilder<S>.() -> Unit)?
    ): LiteralNode<S> {
        return addChildAndApply(LiteralNode(literal, contextRef), apply)
    }

    open fun <T, V> argument(
		argument: CommandArgument<S, T, V>,
		apply: (RequiredArgumentBuilder<S, T>.() -> Unit)?
    ): ArgumentNode<S, T, V> {
        val argumentNode = when (argument) {
            is RequiredArgument -> requiredArgument(argument)
            is OptionalArgument<S, T, *, V> -> optionalArgument(argument)
        }

        return addChildAndApply(argumentNode, null)
    }

    open fun subcommands(vararg commands: Command<S>) {
        subcommands += commands
    }

    protected open fun <T, V> requiredArgument(argument: RequiredArgument<S, T, V>): RequiredArgumentNode<S, T, V> {
        return RequiredArgumentNode(argument, contextRef)
    }

    protected open fun <T, V> optionalArgument(argument: OptionalArgument<S, T, *, V>): OptionalArgumentNode<S, T, V> {
        return OptionalArgumentNode(this, argument, contextRef)
    }

    abstract fun buildNode(): A

    fun buildTree(): A {
        val node = buildNode()

        apply?.invoke(node)

        command?.let { command ->
            node.executes { context ->
                contextRef.context = context
                command.run(context)
            }
        }

        subcommands
            .map(Command<S>::buildLiteral)
            .forEach(node::then)

        children
            .map { it.buildTree() }
            .forEach(node::then)

        return node
    }
}

class LiteralNode<S>(
    private val literal: String,
    contextRef: ContextRef<S>
) : DslCommandTree<S, LiteralArgumentBuilder<S>>(contextRef) {

    override fun buildNode(): LiteralArgumentBuilder<S> {
        return LiteralArgumentBuilder.literal(literal)
    }
}

sealed class ArgumentNode<S, T, V>(
	private val argument: CommandArgument<S, T, V>,
	contextRef: ContextRef<S>
) : DslCommandTree<S, RequiredArgumentBuilder<S, T>>(contextRef) {

    val getter: () -> V = { argument.getValue(contextRef.context) }

    override fun buildNode(): RequiredArgumentBuilder<S, T> {
        return argument.buildArgument()
    }
}

class RequiredArgumentNode<S, T, V>(
	argument: RequiredArgument<S, T, V>,
	contextRef: ContextRef<S>
) : ArgumentNode<S, T, V>(argument, contextRef)

class OptionalArgumentNode<S, T, V>(
	private val parent: DslCommandTree<S, *>,
	argument: OptionalArgument<S, T, *, V>,
	contextRef: ContextRef<S>
) : ArgumentNode<S, T, V>(argument, contextRef) {

    override fun literal(
        literal: String,
        apply: (LiteralArgumentBuilder<S>.() -> Unit)?
    ): Nothing {
        TODO("Only if no command is set")
    }

    override fun <T, V> requiredArgument(argument: RequiredArgument<S, T, V>): Nothing {
        TODO("Only if no command is set")
    }

    override fun <T, V> optionalArgument(argument: OptionalArgument<S, T, *, V>): OptionalArgumentNode<S, T, V> {
        check(command == null) // TODO: Throw exception if executes is set
        return super.optionalArgument(argument)
    }

    override fun executes(command: BrigadierCommand<S>) {
        super.executes(command)
        parent.executes(command)
    }
}

class ContextRef<S> {
    lateinit var context: CommandContext<S>
}
