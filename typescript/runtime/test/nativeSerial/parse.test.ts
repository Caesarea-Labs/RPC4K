test("Parse behavior", () => {
    const json = `
{
   "x": 2,
   "y": "<some-iso-string>",
   "type": "com.example.Data",
   "z": "void"
}
`

    const back = JSON.parse(json, function (key, value)  {
        console.log(`This: ${JSON.stringify(this)}, key: ${key}, value: ${value}`)
        return value
    })
})