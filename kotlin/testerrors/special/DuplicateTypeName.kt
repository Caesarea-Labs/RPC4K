package special
import com.caesarealabs.rpc4k.runtime.api.Api

/**
 * This test is in a special package because it needs to compile alongside package1 and package2 since we are testing having the same type
 * class name exist in different packages.
 */

@Api(true)
abstract class DuplicateTypeName {
    companion object;
    open suspend fun foo(foo1: package1.Foo,foo2: package2.Foo) {

    }
}
