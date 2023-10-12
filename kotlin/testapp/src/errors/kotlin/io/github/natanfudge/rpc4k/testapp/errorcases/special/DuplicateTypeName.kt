
import io.github.natanfudge.rpc4k.runtime.api.ApiClient

/**
 * This test is in a special package because it needs to compile alongside package1 and package2 since we are testing having the same type
 * class name exist in different packages.
 */

@ApiClient
abstract class DuplicateTypeName {
    companion object;
    open suspend fun foo(foo1: io.github.natanfudge.rpc4k.testapp.errorcases.package1.Foo,foo2: io.github.natanfudge.rpc4k.testapp.errorcases.package2.Foo) {

    }
}
