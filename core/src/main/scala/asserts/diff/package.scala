package asserts

package object diff {
  def red(s: String): String = Console.RED + s + Console.RESET
  def green(s: String): String = Console.GREEN + s + Console.RESET
  def blue(s: String): String = Console.BLUE + s + Console.RESET
  def pad(s: Any, i: Int = 5): String = (" " * (i - s.toString.length)) + s
  def arrow(l: String, r: String): String = l + " -> " + r
  def showChange(l: String, r: String): String = red(l) + " -> " + green(r)
}
