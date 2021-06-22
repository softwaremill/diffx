# output

```scala mdoc:invisible
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._
```

`diffx` does its best to show the difference in the most readable way, but obviously the default configuration won't 
cover all the use-cases. Because of that, there are few ways how you can modify its output.

## color & signs

I found it confusing to use the terms `expected`/`actual` as there seems to be no golden rule whether to keep expected on the right side or on the left side.
Because of that, diffx refers to the values that are compared as `left` and `right` value. 

By default, the difference is shown in the following form: 

`leftColor(leftValue) -> rightColor(rightValue)`

which in terms of missing/additional values e.g. in collections looks as follows:

`leftColor(additionalValue)` in case a value was present on the left-hand side and absent on the right side
`rightColor(missingValue)` in case a value was absent on the left-hand side and present on the right side


Where, by default, `rightColor` is green and `leftColor` is red. 

Colors can be customized providing an implicit instance of `ConsoleColorConfig` class.
In fact `rightColor` and `leftColor` are functions `string => string` so they can be modified to do whatever you want with the output.
One example of that would be to use some special characters instead of colors, which might be useful on some environments like e.g. CI.

````scala mdoc:compile-only
val colorConfigWithPlusMinus: ConsoleColorConfig =
ConsoleColorConfig(default = identity, arrow = identity, right = s => "+" + s, left = s => "-" + s)
````

There are two predefined set of colors - light and dark theme. 
The default theme is dark, and it can be changed using environment variable - `DIFFX_COLOR_THEME`(`light`/`dark`).

## skipping identical

In some cases it might be desired to skip rendering the identical fields, to do that simple set `showIgnored` to `false`.

```scala mdoc
case class Person(name:String, age:Int)

val result = compare(Person("Bob", 23), Person("Alice", 23))
output.show(renderIdentical = false)
```