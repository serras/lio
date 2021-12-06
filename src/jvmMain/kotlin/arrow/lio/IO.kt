package arrow.lio

import kotlinx.coroutines.runBlocking

fun <E: Throwable, A> io(f: suspend IO<E>.() -> A): A = runBlocking {
  with(IO<E>(this)) { f() }
}