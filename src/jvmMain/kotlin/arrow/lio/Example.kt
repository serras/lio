package arrow.lio

suspend fun Async<IllegalArgumentException>.program(): String {
  defer { println("What is your name?") }
  val v = defer { readLine()!! }
  if (v == "me") raiseError(IllegalArgumentException("bad name"))
  return v
}

fun main() {
  io<IllegalArgumentException, String> { program() }
}