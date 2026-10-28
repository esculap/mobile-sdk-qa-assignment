package sdk.auth

import org.json.JSONArray
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** contract/auth-contract.json, the same file the iOS test reads. */
fun loadContract(): JSONArray =
    // Gradle runs tests from the android/ folder, so the contract is one level up.
    JSONArray(File("../contract/auth-contract.json").readText())

/** Fake login server: answers with the status from the contract and remembers if it was called. */
class FakeAuthServer(private val status: Int) : AuthServer {
    var wasCalled = false
        private set

    override fun login(username: String, password: String): Int {
        wasCalled = true
        return status
    }
}

class AuthContractTest {
    @Test
    fun loginFollowsContract() {
        val cases = loadContract()
        assertTrue(cases.length() > 0, "Contract file has no cases")

        for (i in 0 until cases.length()) {
            val loginCase = cases.getJSONObject(i)
            val given = loginCase.getJSONObject("given")
            val expected = loginCase.getJSONObject("expected")
            val server = FakeAuthServer(given.getInt("serverStatus"))
            val auth = AuthClient(server)

            val result = auth.login(given.getString("username"), given.getString("password"))

            val label = "${loginCase.getString("id")}: ${loginCase.getString("description")}"
            assertEquals(expected.getString("result"), result.contractName, label)
            assertEquals(expected.getBoolean("serverCalled"), server.wasCalled, "$label (server called)")
        }
    }
}
