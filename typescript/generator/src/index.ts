#!/usr/bin/env node
import {generateClientModel} from "./ClientGenerator";
import * as fs from "fs";
import {createCommand} from "@commander-js/extra-typings";
import path from "path";

// npx rpc4ts -i test/generated/definitions/UserProtocol.rpc.json -o test/generated -d

const options = createCommand()
    // NiceToHave: allow specifying a url to download json from
    .requiredOption("-i, --input <input>", "Path to the api definition json or a directory of them")
    .option("-o --output <output>", "Destination directory for generated sources", __dirname)
    // --dev makes it use relative path for local testing
    .option('-d, --dev', "This option should only be set by the developers of rpc4ts", false)
    .parse()
    .opts()


console.log(`RPC4TS generator v${getGeneratorVersion()}, runtime v${getRuntimeVersion()}`)


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

function getGeneratorVersion(): string {
    // const packageJsonPath = options.dev ? "../package.json" : "./package.json"
    const packageJsonPath = "../package.json"
    const absolutePath = path.join(__dirname, packageJsonPath)
    const packageJson = JSON.parse(fs.readFileSync(absolutePath, "utf-8"))
    return packageJson.version
}

function getRuntimeVersion(): string {
    const nodeModulesPath = options.dev ? "../node_modules" : "../../"
    // Path to the dependency's package.json
    const packageJsonPath = path.join(__dirname, nodeModulesPath, "rpc4ts-runtime", 'package.json');
    const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));

    return packageJson.version;
}

