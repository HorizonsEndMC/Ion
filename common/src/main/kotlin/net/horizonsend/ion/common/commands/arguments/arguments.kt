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

@file:Suppress("unused")

package net.horizonsend.ion.common.commands.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType.bool
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg
import com.mojang.brigadier.arguments.DoubleArgumentType.getDouble
import com.mojang.brigadier.arguments.FloatArgumentType.floatArg
import com.mojang.brigadier.arguments.FloatArgumentType.getFloat
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.LongArgumentType.getLong
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.horizonsend.ion.common.commands.RequiredArgument
import net.horizonsend.ion.common.commands.dsl.DslCommandBuilder

@Throws(CommandSyntaxException::class)
fun error(e: String): Nothing = throw CommandSyntaxException(SimpleCommandExceptionType { "Error" }) { e }

fun <S, T, V> argument(
	name: String, type: ArgumentType<T>,
	getter: (CommandContext<S>, String) -> V
) = RequiredArgument(name, type, getter)

fun <S, T> argumentImplied(
	name: String, type: ArgumentType<T>,
	getter: (CommandContext<S>, String) -> T
) = argument(name, type, getter)

inline fun <S, reified V> impliedGetter(): ((CommandContext<S>, String) -> V) =
	{ context, name -> context.getArgument(name, V::class.java) }

fun <S> DslCommandBuilder<S>.boolean(name: String) = argumentImplied<S, Boolean>(name, bool(), ::getBool)

fun <S> DslCommandBuilder<S>.integer(name: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) =
	argumentImplied<S, Int>(name, integer(min, max), ::getInteger)

fun <S> DslCommandBuilder<S>.long(name: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE) =
	argumentImplied<S, Long>(name, longArg(min, max), ::getLong)

fun <S> DslCommandBuilder<S>.float(name: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE) =
	argumentImplied<S, Float>(name, floatArg(min, max), ::getFloat)

fun <S> DslCommandBuilder<S>.double(name: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE) =
	argumentImplied<S, Double>(name, doubleArg(min, max), ::getDouble)

fun <S> DslCommandBuilder<S>.word(name: String) = argumentImplied<S, String>(name, word(), ::getString)
fun <S> DslCommandBuilder<S>.string(name: String) = argumentImplied<S, String>(name, string(), ::getString)
fun <S> DslCommandBuilder<S>.greedyString(name: String) = argumentImplied<S, String>(name, greedyString(), ::getString)
