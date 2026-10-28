package sdk.auth

// Stand-in for an SDK authentication module. In the real SDK this code already exists;
// it is here only so the test has something to run against.

/** What the SDK promises to every consuming app. Names must match iOS. */
enum class LoginResult(val contractName: String) {
    SUCCESS("success"),
    INVALID_INPUT("invalidInput"),
    INVALID_CREDENTIALS("invalidCredentials"),
    SERVICE_UNAVAILABLE("serviceUnavailable"),
}

/** Talks to the login server and returns the HTTP status. Tests replace it with a fake. */
fun interface AuthServer {
    fun login(username: String, password: String): Int
}

class AuthClient(private val server: AuthServer) {
    fun login(username: String, password: String): LoginResult {
        // Checked in the app, without calling the server
        if (username.isEmpty() || password.isEmpty()) {
            return LoginResult.INVALID_INPUT
        }

        // Checked by the server
        return when (server.login(username, password)) {
            200 -> LoginResult.SUCCESS
            401 -> LoginResult.INVALID_CREDENTIALS
            else -> LoginResult.SERVICE_UNAVAILABLE
        }
    }
}
