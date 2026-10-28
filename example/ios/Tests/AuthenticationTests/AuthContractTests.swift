import Foundation
import XCTest
@testable import Authentication

/// One case from contract/auth-contract.json, the same file the Android test reads.
struct LoginCase: Decodable {
    struct Given: Decodable {
        let username: String
        let password: String
        let serverStatus: Int
    }
    struct Expected: Decodable {
        let result: String
        let serverCalled: Bool
    }

    let id: String
    let description: String
    let given: Given
    let expected: Expected
}

func loadContract() throws -> [LoginCase] {
    let exampleFolder = URL(fileURLWithPath: #filePath)
        .deletingLastPathComponent() // AuthenticationTests
        .deletingLastPathComponent() // Tests
        .deletingLastPathComponent() // ios
        .deletingLastPathComponent() // example
    let file = exampleFolder.appendingPathComponent("contract/auth-contract.json")
    return try JSONDecoder().decode([LoginCase].self, from: Data(contentsOf: file))
}

/// Fake login server: answers with the status from the contract and remembers if it was called.
final class FakeAuthServer: AuthServer {
    let status: Int
    private(set) var wasCalled = false

    init(status: Int) {
        self.status = status
    }

    func login(username: String, password: String) -> Int {
        wasCalled = true
        return status
    }
}

final class AuthContractTests: XCTestCase {
    func testLoginFollowsContract() throws {
        let cases = try loadContract()
        XCTAssertFalse(cases.isEmpty, "Contract file has no cases")

        for loginCase in cases {
            let server = FakeAuthServer(status: loginCase.given.serverStatus)
            let auth = AuthClient(server: server)

            let result = auth.login(username: loginCase.given.username, password: loginCase.given.password)

            let label = "\(loginCase.id): \(loginCase.description)"
            XCTAssertEqual(result.rawValue, loginCase.expected.result, label)
            XCTAssertEqual(server.wasCalled, loginCase.expected.serverCalled, "\(label) (server called)")
        }
    }
}
