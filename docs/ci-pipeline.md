# CI/CD pipeline (GitHub Actions)

The only workflow that actually runs in this repository is [.github/workflows/contract-tests.yml](../.github/workflows/contract-tests.yml), the contract tests from the example. This page sketches the full pipeline I would build for the SDK. The sketch is **illustrative**: module names, schemes and scripts are placeholders, since the real repository layout is unknown.

## Stages

| Stage | Trigger | What runs | Target time | Blocks |
| --- | --- | --- | --- | --- |
| **PR checks** | Every pull request | Build both platforms, unit + contract tests, API-diff check, integration tests of affected modules, a few app smoke tests on a simulator/emulator | ~15 min | Merge |
| **Nightly** | Every night on `main` | All integration tests, full XCUITest/Espresso suite on real devices (device cloud), performance metrics, a few journeys against the real test environment | 1–2 h | Release, until triaged |
| **Release candidate** | Tag `vX.Y.Z-rc` | Everything required on the exact candidate version, compatibility matrix, consuming-app smoke tests | Hours | Publishing |
| **Publish** | Manual approval after RC is green | Publish the version, changelog and upgrade notes, and notify consuming teams | — | — |

## Sketch: PR checks

```yaml
# .github/workflows/pr-checks.yml  (sketch)
name: PR checks
on: pull_request

jobs:
  changes:                     # Which modules did this PR touch?
    runs-on: ubuntu-latest
    outputs:
      auth: ${{ steps.filter.outputs.auth }}
      payments: ${{ steps.filter.outputs.payments }}
    steps:
      - uses: actions/checkout@v4
      - uses: dorny/paths-filter@v3
        id: filter
        with:
          filters: |
            auth:     ['modules/auth/**', 'contracts/auth/**']
            payments: ['modules/payments/**', 'modules/auth/**']   # payments depends on auth

  ios:
    needs: changes
    runs-on: macos-15
    steps:
      - uses: actions/checkout@v4
      - name: Unit + contract tests
        run: xcodebuild test -scheme SDK -testPlan PRChecks -destination 'platform=iOS Simulator,name=iPhone 16'
      - name: API-diff check (breaking public API changes)
        run: swift package diagnose-api-breaking-changes origin/main
      - name: Integration tests for affected modules
        if: needs.changes.outputs.auth == 'true'
        run: xcodebuild test -scheme TestHost -only-testing:IntegrationTests/Auth -retry-tests-on-failure -test-iterations 2
      - name: Keep results for debugging
        if: always()
        uses: actions/upload-artifact@v4
        with: { name: ios-results, path: '**/*.xcresult' }

  android:
    needs: changes
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17' }
      - name: Unit + contract tests, API-diff check
        run: ./gradlew testDebugUnitTest apiCheck
      - name: Integration tests for affected modules (emulator)
        if: needs.changes.outputs.auth == 'true'
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          script: ./gradlew :testhost:connectedDebugAndroidTest
      - name: Keep results for debugging
        if: always()
        uses: actions/upload-artifact@v4
        with: { name: android-results, path: '**/build/reports/**' }

  report:                      # One short PR comment with what failed and where to look
    needs: [ios, android]
    if: always()
    runs-on: ubuntu-latest
    steps:
      - run: ./scripts/post-pr-summary.sh   # failed tests, platform, module versions, links to artifacts
```

## Sketch: nightly and release candidate

```yaml
# .github/workflows/nightly.yml  (sketch)
on:
  schedule: [{ cron: '0 2 * * *' }]
jobs:
  preflight:     # test environment and test data available? If not, stop in seconds and report an environment problem
  ios-full:      # all integration tests + full XCUITest suite on real devices (device cloud)
  android-full:  # all integration tests + full Espresso suite on real devices (device cloud)
  performance:   # SDK init time, app start time, memory; compared with the previous release
  real-backend:  # a few key journeys against the test environment instead of stubs
  notify:        # summary to the team channel: new failures, known failures, owners

# .github/workflows/release-candidate.yml  (sketch)
on:
  push:
    tags: ['v*-rc*']
jobs:
  required-suites:       # everything required, on this exact version, both platforms
  compatibility-matrix:  # candidate + new app, + each consuming app, upgrade from previous version, min/max OS
  consumer-smoke:        # build each consuming app with the candidate and run its smoke journeys
  release-report:        # version, what was tested, matrix results, known issues, upgrade notes
  publish:
    needs: [required-suites, compatibility-matrix, consumer-smoke]
    environment: release   # manual approval by the release owner
```

## Rules the pipeline enforces

- **Merge** requires every PR check to pass on both platforms. An Android pass never replaces a missing or failing iOS result, and the reverse is also true.
- **One automatic retry.** A test that passes on retry is reported as flaky and gets a ticket.
- **Quarantined tests** are listed in one file, each with an owner, a ticket and a deadline. Critical flows are quarantined only if another test covers the same risk.
- **Every failure keeps its evidence:** `.xcresult` bundles, Android test reports, logs and module versions are uploaded as artifacts, even when the job fails.
