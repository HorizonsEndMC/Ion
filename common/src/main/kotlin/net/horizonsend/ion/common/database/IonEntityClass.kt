package net.horizonsend.ion.common.database

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SizedIterable

// Challenge: Count how many times we use generics here

/** Wrapper of EntityClass to work around some annoying things with it's API */
open class IonEntityClass<I : Comparable<I>, out E : Entity<I>>(val inner: EntityClass<I, E>) {
	constructor(table: IdTable<I>) : this(object : EntityClass<I, E>(table) {})

	operator fun get(id: I): E? = inner.findById(id)

	fun find(op: Op<Boolean>): SizedIterable<E> = inner.find(op)

	fun new(id: I?, init: E.() -> Unit): E = inner.new(id, init)

	fun new(init: E.() -> Unit): E = inner.new(init)

	infix fun <REF : Comparable<REF>> referencedOn(column: Column<REF>) = inner.referencedOn(column)

	infix fun <TI : Comparable<TI>, TE : Entity<TI>, R : Comparable<R>> IonEntityClass<TI, TE>.referrersOn(column: Column<R>)
		= this@IonEntityClass.inner.run { this@referrersOn.inner.referrersOn(column) }
}
