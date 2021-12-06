package arrow.lio

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext

data class IO<E: Throwable>(val scope: CoroutineScope): Async<E> {
  override suspend fun <A> defer(f: () -> A): A = f()
  override suspend fun raiseError(e: E): Nothing = throw e
  override suspend fun cancel() = scope.cancel()
  override suspend fun <A> uncancelable(f: (Poll<A>) -> A): A {
    val poll = Poll<A> { g -> withContext(scope.coroutineContext) { g() } }
    return withContext(NonCancellable) { f(poll) }
  }
  override suspend fun <A> (() -> A).start(): Fiber<E, A> =
    DefFiber(scope.async { this@start() })
}

data class DefFiber<E, A>(val fiber: Deferred<A>): Fiber<E, A> {
  override suspend fun cancel() = fiber.cancel()
  override suspend fun join(): Outcome<E, A> = try {
    Succeeded(fiber.await())
  } catch (e: CancellationException) {
    Canceled(e)
  } catch (e: Throwable) {
    Errored(e as E)
  }
}