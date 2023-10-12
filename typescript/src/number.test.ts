

test('double', async () => {
  const res = await fetch("https://google.com")
  console.log(await res.text())
  expect(1 + 1).toBe(2)
});

