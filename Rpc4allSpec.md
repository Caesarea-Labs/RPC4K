## Types
### void
| Kotlin   | Typescript          |
|----------|---------------------|
| `Unit`   | `void` or `"void"`* | 

* In Typescript, `void` is denoted as `"void"` (string literal type) usually, but on API return types `void` is interpreted as Typescript `void` ("returns no value" type)

`void` denotes "something, but nothing in particular", same as the kotlin `Unit`. 

Any value is valid as `void`, as consumers of a `void` value will simply ignore it. 
Note that unlike Typescript `void`, undefined is not a valid value because undefined is a lack of value. 