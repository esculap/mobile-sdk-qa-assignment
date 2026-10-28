import Foundation

// Stand-in for an SDK authentication module. In the real SDK this code already exists;
// it is here only so the test has something to run against.

/// What the SDK promises to every consuming app. Names must match Android.
public enum LoginResult: String {
    case success
    case invalidInput
    case invalidCredentials
    case serviceUnavailable
}

/// Talks to the login server and returns the HTTP status. Tests replace it with a fake.
public protocol AuthServer {
    func login(username: String, password: String) -> Int
}

public struct AuthClient {
    private let server: any AuthServer

    public init(server: any AuthServer) {
        self.server = server
    }

    public func login(username: String, password: String) -> LoginResult {
        // Checked in the app, without calling the server
        if username.isEmpty || password.isEmpty {
            return .invalidInput
        }

        // Checked by the server
        switch server.login(username: username, password: password) {
        case 200: return .success
        case 401: return .invalidCredentials
        default: return .serviceUnavailable
        }
    }
}
