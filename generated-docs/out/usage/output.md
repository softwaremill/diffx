# output


`diffx` does its best to show the difference in the most readable way, but obviously the default configuration won't 
cover all the use-cases. Because of that, there are few ways how you can modify its output.

## intellij idea

If you are running tests using **IntelliJ IDEA**'s test runner, you will want
to turn off the red text coloring it uses for test failure outputs because
it interferes with difflicious' color outputs.

In <b>File | Settings | Editor | Color Scheme | Console Colors | Console | Error Output</b>, uncheck the red foreground color.
(Solution provided by Jacob Wang @jatcwang)

## colors & signs

Diffx refers to the values that are compared as `left` and `right`, but you can think of them as `actual` and `expected`. 

By default, the difference is shown in the following form: 

`leftColor(leftValue) -> rightColor(rightValue)`

When comparing collection types the difference is calculated against the `right` value

`additionalColor(additionalValue)` when there is an additional entity on the left-hand side
`missingColor(missingValue)` when there is a missing entity on the left-hand side 

Colors can be customized providing an implicit instance of `ShowConfig` class.
In fact `rightColor` and `leftColor` are functions `string => string` so they can be modified to do whatever you want with the output.
One example of that would be to use some special characters instead of colors, which might be useful on some environments like e.g. CI.

````scala
val showConfigWithPlusMinus: ShowConfig =
    ShowConfig.default.copy(default = identity, arrow = identity, right = s => "+" + s, left = s => "-" + s)
````

There are two predefined set of colors - light and dark theme. 
The default theme is dark, and it can be changed using environment variable - `DIFFX_COLOR_THEME`(`light`/`dark`).

## skipping identical

In some cases it might be desired to skip rendering identical fields.
This can be achieved by using a specific DiffResult transformer. DiffResult transformer is a part of `ShowConfig`.
By default, it is set to an identical function.

```scala
implicit val showConfig = ShowConfig.default.copy(transformer = DiffResultTransformer.skipIdentical)
// showConfig: ShowConfig = ShowConfig(
//   left = com.softwaremill.diffx.ShowConfig$$$Lambda$9509/0x00000008023f7380@138872c6,
//   right = com.softwaremill.diffx.ShowConfig$$$Lambda$9509/0x00000008023f7380@218ffbee,
//   missing = com.softwaremill.diffx.ShowConfig$$$Lambda$9509/0x00000008023f7380@62ec1f1b,
//   additional = com.softwaremill.diffx.ShowConfig$$$Lambda$9509/0x00000008023f7380@4b736101,
//   default = com.softwaremill.diffx.ShowConfig$$$Lambda$9512/0x00000008023f7b90@1bbd34eb,
//   arrow = com.softwaremill.diffx.ShowConfig$$$Lambda$9509/0x00000008023f7380@4533fcf9,
//   transformer = com.softwaremill.diffx.DiffResultTransformer$$$Lambda$9501/0x00000008023f5358@47a52783
// )
case class Person(name:String, age:Int)

val result = compare(Person("Bob", 23), Person("Alice", 23))
// result: DiffResult = DiffResultObject(
//   name = "Person",
//   fields = ListMap(
//     "name" -> DiffResultString(
//       diffs = List(
//         DiffResultStringLine(
//           diffs = List(DiffResultValue(left = "Bob", right = "Alice"))
//         )
//       )
//     ),
//     "age" -> IdenticalValue(value = 23)
//   )
// )
result.show()
// res1: String = """Person(
//      name: Bob -> Alice)"""
```

There is a convenient method in `ShowConfig` called `skipIdentical` which does exactly that, so the relevant line from 
the example can be shortened to `ShowConfig.default.skipIdentical`