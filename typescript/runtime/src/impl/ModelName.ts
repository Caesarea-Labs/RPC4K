import dayjs, {Dayjs} from "dayjs";
import {RpcType} from "./ApiDefinition";
import {RpcTypeNames} from "./RpcTypeUtils";
import {removeBeforeLastExclusive} from "./Util";




/**
 * Converts the Rpc representation of a struct name of a form "Foo.Bar" to a form "Bar".
 */
export function simpleModelName(name: string): string {
    // Treat "Foo.Bar" as "Bar"
    return removeBeforeLastExclusive(name, ".")
}