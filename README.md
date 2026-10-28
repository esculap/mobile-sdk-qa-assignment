# Mobile SDK Quality Engineer: technical assignment

Illia Andrieiev

## What is in this repository

| Path | Content |
| --- | --- |
| [docs/quality-strategy.md](docs/quality-strategy.md) | **The main document**: test strategy, automation example summary, CI/CD and release validation, investigation scenario, initial priorities, assumptions and trade-offs |
| [example/](example/README.md) | Automation example: a login contract (4 cases) tested natively on iOS (XCTest) and Android (JUnit) from one shared contract file |
| [docs/ci-pipeline.md](docs/ci-pipeline.md) | Pipeline stages and a GitHub Actions sketch for PR, nightly and release-candidate checks |
| [.github/workflows/contract-tests.yml](.github/workflows/contract-tests.yml) | The workflow that runs: the example's tests on iOS and Android on every PR |
| [docs/questions-for-the-team.md](docs/questions-for-the-team.md) | Questions I would ask before implementing the full solution |

## How to run the example

```bash
# iOS (needs Xcode)
cd example/ios && swift test

# Android (needs JDK 17 and Gradle 8.x)
cd example/android && gradle test
```

Both suites also run in GitHub Actions on every push and pull request. See the Actions tab.

## Main design choices

- **Most confidence from fast, low-level tests** (unit, contract, module integration), and a thin layer of end-to-end tests. This addresses slow, unreliable E2E tests and late, hard-to-trace integration failures.
- **Share the expected behaviour, not the test code.** One contract file per module, read by native iOS and Android tests. This makes iOS/Android differences visible as test failures.
- **Native test frameworks** (XCTest/XCUITest, JUnit/Espresso), because they are fast, stable and maintainable by the developers who own the modules.
- **Compatibility as an explicit promise:** semantic versioning, an API-diff check on every PR, and a compatibility matrix with consuming apps for every release candidate.
- **Flaky tests are visible:** at most one retry, flaky results reported as flaky, and quarantine only with an owner, a ticket and a deadline.

## Assumptions

The assignment leaves these open, so I assumed:

- SDK modules are native Swift/Kotlin code, versioned, and consumed by several apps.
- GitHub and GitHub Actions are used for code and CI. The approach works the same in other CI systems.
- A device cloud for real-device runs is available or can be added.
- Modules let tests replace the real server with a fake. If a module doesn't, I'd ask developers to add this. 
- Unit tests are written and owned by developers. QA makes sure they run in the pipeline, so this repository has no unit-test example.

## Known limitations

- The example's `AuthClient` is a small stand-in for a real SDK module, written only so the tests have something to run against.
- The example is synchronous for readability. A real login is async, and the tests would use `async`/`await` (iOS) or coroutines `runTest` (Android).
- The Android example is a plain Kotlin/JVM module rather than an Android library module, so it runs without the Android SDK. The tests themselves would be the same.
- The full pipeline in `docs/ci-pipeline.md` is a sketch with placeholder module names. Only the contract-tests workflow runs.
- Integration, UI and release-validation layers are described, not implemented.

## With more time I would

- Add more contract cases, for example a blocked account or too many attempts.
- Add a module-integration example: the authentication module and a second module that needs the logged-in user, tested together in a small test host app.
- Run the same contract cases against a WireMock stub server, using the real HTTP stack.
- Add async versions of the tests.
- Build a simple dashboard for test duration and flaky rate per suite.

## AI use

**Where:** I used AI assistants (OpenAI Codex and Anthropic Claude) to analyse the assignment, draft and restructure the strategy document, and write the example code and pipeline sketch.

**How I validated it:**

- I read everything the AI drafted. For each section of the assignment, I first thought about how I would answer it myself. Then I compared my answer with the AI draft.
- Where they differed, I asked the AI to rewrite the section based on my thinking and my experience. I removed parts that I found redundant or unnecessary.
- I read all the code: the tests, the contract JSON file and the GitHub Actions YAML files.
- I ran the tests in GitHub Actions.
- I changed the tests. Some tests the AI proposed were unnecessary, and others did not show clearly enough what the example is meant to show.

**What I changed or decided myself:**

- I chose authentication as the capability for the automation example.
- I chose GitHub Actions for CI. I understand your team may use Azure DevOps. GitHub Actions is simpler for a public example, and I have more experience with it. I don't expect any problems adding tests and building the pipeline in Azure DevOps.
- I reworked the strategy decisions.
- I added my own experience of how tests are triggered in CI: which run on each PR and which after merge, based on my previous projects.
