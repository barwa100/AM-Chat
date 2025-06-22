package pwr.barwa.chat.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pwr.barwa.chat.data.responses.TokenResponse
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import pwr.barwa.chat.data.dto.UserDto

class AuthService {
    companion object {

        const val URL_BASE: String = "http://51.75.129.73:5000/"
//        const val URL_BASE: String = "http://10.0.2.2:5000/" // For Android emulator, use localhost as base URL
        private var token: String? = null
    }

    suspend fun login(username: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            val url = URL(URL_BASE + "Auth/login")
            val connection = url.openConnection() as HttpURLConnection

            return@withContext try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val requestBody = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody)
                    writer.flush()
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    token = jsonResponse.getString("accessToken")
                    Result.success(
                        TokenResponse(
                            tokenType = jsonResponse.optString("tokenType"),
                            accessToken = token!!,
                            expiresIn = jsonResponse.getLong("expiresIn"),
                            refreshToken = jsonResponse.getString("refreshToken")
                        )
                    )
                } else if (connection.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    val errorResponse =
                        connection.errorStream.bufferedReader().use { it.readText() }
                    val jsonError = JSONObject(errorResponse)
                    Result.failure(Exception("Incorrect username or password."))
                } else {
                    val errorResponse =
                        connection.errorStream.bufferedReader().use { it.readText() }
                    Result.failure(Exception("Unexpected error: ${connection.responseCode}, $errorResponse"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection.disconnect()
            }
        }
    }
    suspend fun register(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val url = URL(URL_BASE + "Auth/register")
            val connection = url.openConnection() as HttpURLConnection

            return@withContext try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val requestBody = JSONObject().apply {
                    put("username", email)
                    put("password", password)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody)
                    writer.flush()
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    Result.success(Unit)
                } else if (connection.responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    val errorResponse =
                        connection.errorStream.bufferedReader().use { it.readText() }
                    val jsonError = JSONObject(errorResponse)
                    val errors =
                        jsonError.getJSONArray("errors")?.toString(2) ?: "Unknown validation error"
                    Result.failure(Exception("Validation error: $errors"))
                } else {
                    Result.failure(Exception("Unexpected error: ${connection.responseCode}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun getUserByUsername(username: String): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            val url = URL(URL_BASE + "Auth/me")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    // Parsowanie JSON na UserDto (przykład, zmodyfikuj wg struktury JSON)
                    val user = UserDto(
                        id = json.getLong("id"),
                        userName = json.getString("userName"),
                        avatarUrl = if (json.isNull("avatarUrl")) null else json.getString("avatarUrl"),
                        channels = json.getJSONArray("channels").let { array ->
                            List(array.length()) { i -> array.getLong(i) }
                        },
                        messages = json.getJSONArray("messages").let { array ->
                            List(array.length()) { i -> array.getLong(i) }
                        },
                        contacts = json.getJSONArray("contacts").let { array ->
                            List(array.length()) { i -> array.getLong(i) }
                        }
                    )

                    Result.success(user)
                } else {
                    val error = connection.errorStream.bufferedReader().use { it.readText() }
                    Result.failure(Exception("Failed to get user: HTTP ${connection.responseCode}, $error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection.disconnect()
            }
        }
    }
}