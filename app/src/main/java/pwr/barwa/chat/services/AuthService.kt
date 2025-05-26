package pwr.barwa.chat.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pwr.barwa.chat.data.responses.TokenResponse
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuthService {
    companion object {
        const val URL_BASE: String = "http://51.75.129.73:5000/"
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

                    Result.success(
                        TokenResponse(
                            tokenType = jsonResponse.optString("tokenType"),
                            accessToken = jsonResponse.getString("accessToken"),
                            expiresIn = jsonResponse.getLong("expiresIn"),
                            refreshToken = jsonResponse.getString("refreshToken")
                        )
                    )
                } else if (connection.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    val errorResponse =
                        connection.errorStream.bufferedReader().use { it.readText() }
                    val jsonError = JSONObject(errorResponse)
                    Result.failure(Exception("Podano błędne dane logowania."))
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
}