import {BooleanSerializer, NumberSerializer, RpcModel, SerialDescriptor, StringSerializer} from "rpc4ts-runtime";
import {CodeBuilder} from "./CodeBuilder";
import {buildClassSerialDescriptor} from "rpc4ts-runtime/src/serialization/descriptors/SerialDescriptors";
import {TsSerializer} from "rpc4ts-runtime/src/serialization/core/TsSerializer";
import {Encoder} from "rpc4ts-runtime/src/serialization/core/encoding/Encoder";
import {Decoder, DECODER_DECODE_DONE} from "rpc4ts-runtime/src/serialization/core/encoding/Decoding";
import {Foo} from "rpc4ts-runtime/test/serialization/JsonToStringTest.test";
import {ArraySerializer} from "rpc4ts-runtime/src/serialization/internal/CollectionSerializers";

/**
 *
 * @param models
 */
export function generateSerializers(models: RpcModel[]) {
    const builder = new CodeBuilder()


}

