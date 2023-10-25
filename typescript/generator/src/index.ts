#!/usr/bin/env node
import {generateClientModel} from "./ClientGenerator";
import * as fs from "fs";
import {createCommand} from "@commander-js/extra-typings";

// npx rpc4ts -i test/generated/definitions/UserProtocol.rpc.json -o test/generated -d

const options = createCommand()
    // --dev makes it use relative path for local testing
    // NiceToHave: allow specifying a url to download json from
    .requiredOption("-i, --input <input>", "Path to the api definition json or a directory of them")
    .option("-o --output <output>", "Destination directory for generated sources", __dirname)
    .option('-d, --dev', "This option should only be set by the developers of rpc4ts", false)
    .parse()
    .opts()


if (!fs.existsSync(options.input)) {
    throw new Error(`No file exists at specified input path '${options.input}'`)
}

if (fs.existsSync(options.output)) {
    if (!fs.statSync(options.output).isDirectory()) {
        throw new Error("The destination path should be a directory")
    } else {
        // Delete old files
        for (const file of fs.readdirSync(options.output)) {
            if (file.startsWith("rpc4ts_")) {
                fs.rmSync(options.output + "/" + file)
            }
        }
    }
}

console.log(`Generating typescript to ${options.output}`)


if (fs.statSync(options.input).isDirectory()) {
    for (const file of fs.readdirSync(options.input)) {
        generateForFile(options.input + "/" + file)
    }
} else {
    generateForFile(options.input)
}

function generateForFile(file: string) {
    generateClientModel(
        fs.readFileSync(file).toString(),
        options.output,
        {localLibPaths: options.dev}
    )
}
