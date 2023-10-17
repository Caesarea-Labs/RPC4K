#!/usr/bin/env ts-node
import {generateClientModel} from "./ClientGenerator";
import * as fs from "fs";

generateClientModel(
    fs.readFileSync("test/generated/definitions/UserProtocol.rpc.json").toString(),
    "test/generated/",
    {localLibPaths: true}
)
