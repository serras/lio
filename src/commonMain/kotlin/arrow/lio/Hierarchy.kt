package arrow.lio

import kotlinx.coroutines.CancellationException

interface Suspend {
  suspend fun <A> defer(f: () -> A): A
}

interface Error<E> {
  suspend fun raiseError(e: E): Nothing
}

interface Cancel<E>: Error<E> {
  suspend fun cancel(): Unit
  suspend fun <A> uncancelable(f: (Poll<A>) -> A): A
}

fun interface Poll<A> {
  suspend fun apply(f: () -> A): A
}

interface Spawn<E>: Cancel<E> {
  suspend fun <A> (() -> A).start(): Fiber<E, A>
}

sealed interface Outcome<out E, out A>
data class Canceled(val e: CancellationException): Outcome<Nothing, Nothing>
data class Errored<out E>(val e: E): Outcome<E, Nothing>
data class Succeeded<out A>(val x: A): Outcome<Nothing, A>

interface Fiber<E, A> {
  suspend fun cancel(): Unit
  suspend fun join(): Outcome<E, A>
}

interface Sync<E>: Suspend, Cancel<E>
interface Async<E>: Sync<E>, Spawn<E>