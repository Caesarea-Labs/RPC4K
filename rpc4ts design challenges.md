# Rpc4ts Codegen Design Challenges
TODO: make this more robust:
Basically, we need to juggle the following requirements:
- Allow differentiating between union type children
- For defaulted properties in server models, allow constructing objects with a missing value, but expose a non-nullable value 
for the same property when the value has being received from the server.
... example...
- Have runtime information that is good enough to resolve union types in typescriptx.serialization. 
- Avoid people creating models with {} literals (solution: branding, or adding more methods)


# Rpc4ts Runtime Design challenges
TODO: Detail the choice of copying kotlinx.serialization over using json.stringify...