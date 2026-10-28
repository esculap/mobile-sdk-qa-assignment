# Questions for the team

Before building the full solution, I would ask these questions. The answers change what I would build first.

## SDK and modules

1. **How do apps get the SDK modules today?** As versioned packages that each app chooses a version of, or as source code in the same repository as the app? This decides what "module version 3.5" means in practice and how we test compatibility.
2. **How many modules are there, and who owns each one?** Is there a dependency map, even an informal one?
3. **Are the modules fully native (Swift/Kotlin), or is any shared code used,** such as Kotlin Multiplatform?
4. **Which consuming apps use the SDK, and how quickly do they adopt new versions?** How many SDK versions back do we need to support?

## Testing and CI today

5. **What automation exists today, and which parts do people trust?** Which tests are flaky or slow?
6. **Which CI system is used, and do we have a device cloud** (BrowserStack, Firebase Test Lab) or physical devices for iOS and Android?
7. **Do modules have test host or sample apps,** and can the backend be replaced with a fake or stub in tests?
8. **Is there a stable test environment and test accounts** for the backend, or a mock server setup?

## Releases and process

9. **How is an SDK release made today, and who decides that it is ready?** Is there a changelog or upgrade guide for consuming teams?
10. **How did recent integration bugs show up, and how long did they take to trace?** One or two real examples would show where the biggest gaps are.
11. **Which OS versions and devices must be supported** (lowest iOS and Android versions)?
12. **How do teams communicate failures today:** a channel, tickets, PR comments?
