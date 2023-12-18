import dayjs from "dayjs";

test("Stringify behavior", () => {
    // console.log(JSON.stringify({
    //     x: undefined
    // }, function (key, value) {
    //     console.log(`This: ${JSON.stringify(this)}, key: ${key}, value: ${JSON.stringify(value)}`)
    // }))
})

test("Stringify undefined top level", () => {
    // const x: void = undefined
    // console.log(JSON.stringify(undefined, function (key, value) {
    //     console.log(`This: ${JSON.stringify(this)}, key: ${key}, value: ${JSON.stringify(value)}`)
    // }))
})

test("Stringify key value behavior", () => {
    const y = dayjs()
    const obj = {
        x: 2,
        y,
        type: "Data",
        z: "void"
    }

    const map: Record<string,string> = {
        "Data": "com.example.Data"
    }

    const stringified = JSON.stringify(obj, (key, value) => {
        if(key === "type") {
            return map[value] ?? value
        } else {
            return value
        }
    })

    expect(stringified).toEqual(`{"x":2,"y":"${y.toISOString()}","type":"com.example.Data","z":"void"}`)
})