# Automation example: login contract, tested natively on iOS and Android

## The problem it addresses

Two challenges from the assignment: "behaviour is not always consistent between iOS and Android", and "module updates can unintentionally affect consuming apps". Login is a good place to show both. Suppose iOS reports a wrong password as "service unavailable" while Android reports it correctly. Then every consuming app shows different messages, and nobody notices until a customer complains.

## The idea in three sentences

1. The expected login behaviour is written down once, in [contract/auth-contract.json](contract/auth-contract.json): for each situation (given), what the SDK must return (expected).
2. iOS (XCTest) and Android (JUnit) each have **one short test** that reads **the same file** and checks its own platform's SDK code against it.
3. The login server is replaced with a fake, so the tests need no backend, device or emulator and run in seconds on every pull request.

```
contract/auth-contract.json   ← single source of truth
        │
        ├── ios/      XCTest  → AuthClient.swift
        └── android/  JUnit   → AuthClient.kt
                      (both with a fake login server)
```

## The four cases

| Case | Given | Expected result | Server called? |
| --- | --- | --- | --- |
| AUTH-01 | Correct username and password | `success` | yes |
| AUTH-02 | Empty password | `invalidInput`, rejected in the app | **no** |
| AUTH-03 | Wrong password, server answers 401 | `invalidCredentials` | yes |
| AUTH-04 | Login service down, server answers 503 | `serviceUnavailable` | yes |

The test checks two things for each case: the **result** the app receives, and **whether the server was called**. The second check is what separates a validation done in the app (AUTH-02) from one done by the server (AUTH-03).

To add a rule, for example "account blocked → `accountBlocked`", you add one entry to the JSON file. Both platforms then fail until both implement it. Parity is checked by the tests, not by someone comparing two apps by hand.

## What is real and what is fake

| Part | In this example | In the real SDK |
| --- | --- | --- |
| `AuthClient` (login logic) | Small stand-in, about 30 lines | The existing auth module, tested as is |
| `AuthServer` | Fake that returns the status from the JSON and remembers whether it was called | Real HTTP call to the login backend |

The stand-in `AuthClient` exists only so the test has something to run against. The contract file and the test are the parts that matter.

## How this differs from a unit test

It uses the same tools as unit tests (XCTest, JUnit), but it checks something different:

- **A unit test** checks the module's internal code. Developers write and own it.
- **This contract test** checks only the module's **public promise to consuming apps**: what result an app gets in each situation. The expected results live in one file shared by iOS and Android and agreed with consuming teams. QA reviews it. A change to the contract is a change to the SDK's behaviour, so it should be visible in a pull request.

## What this test does NOT cover, and where that is covered instead

- **The real backend and HTTP stack**: integration tests against a stub server such as WireMock, plus a few tests against the test environment.
- **The login module working with other modules**, for example a module that needs the logged-in user: module-integration tests in a small test host app.
- **What the user sees** (login screen, error messages): a few XCUITest and Espresso tests in the new app, plus exploratory testing.
- **Async behaviour**: the stand-in is synchronous to keep it readable. A real login call is async, so on iOS the test would use `async`/`await` and on Android `runTest` with coroutines. The contract idea stays the same.

## How to run

The workflow [.github/workflows/contract-tests.yml](../.github/workflows/contract-tests.yml) runs both tests on every pull request.

Locally:

```bash
# iOS (needs Xcode)
cd example/ios && swift test

# Android (needs JDK 17 and Gradle 8.x)
cd example/android && gradle test
```

To see a failure, change `case 401: return .invalidCredentials` in `ios/Sources/Authentication/AuthClient.swift` to `.serviceUnavailable` and run `swift test`. The test reports `AUTH-03: Wrong password is rejected by the server`.
