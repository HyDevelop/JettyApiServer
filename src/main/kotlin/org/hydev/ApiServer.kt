package org.hydev

import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponse.*
import org.eclipse.jetty.server.Server
import java.lang.System.err


/**
 * TODO: Write a description for this class!
 *
 * @author HyDEV Team (https://github.com/HyDevelop)
 * @author Hykilpikonna (https://github.com/hykilpikonna)
 * @author Vanilla (https://github.com/VergeDX)
 * @since 2020-07-03 12:31
 */
class ApiServer(
    val port: Int
) {
    // Registered nodes
    val nodes = ApiNodeManager()

    // Jetty handler
    val jettyHandler = JettyHandler(this)

    // Jetty server
    val jetty = Server(port).apply { handler = jettyHandler }

    // Error handlers
    var handleNullRequest: () -> Unit =
        { err.println("Error: Somehow a Jetty parameter is null when handle is called") }
    var handleWrongMethod: (HttpAccess) -> Unit = { it.write(SC_BAD_REQUEST, jsonError("Invalid method.")) }
    var handleNodeNotFound: (HttpAccess) -> Unit = { it.write(SC_NOT_FOUND, jsonError("Not found.")) }
    var handleSuppressedError: (ApiAccess, Exception) -> Unit = { access: ApiAccess, e: Exception ->
        println("Ignored error: ${e.message}")
        access.write(jsonError(e.message))
    }
    var handleError: (ApiAccess, Exception) -> Unit = { access: ApiAccess, e: Exception ->
        err.println("============= ERROR =============\n${access}")
        e.printStackTrace()
        err.println("-------------- END --------------")
        access.write(jsonError(e.message))
    }

    // Suppressed errors
    var isSuppressed: (Exception) -> Boolean = { it is KnownException }

    // Accepted methods
    var acceptedMethods = mutableListOf("get", "post")

    // Configure default response
    var configureResponse: (HttpServletResponse) -> Unit = {
        it.status = SC_OK
        it.contentType = "application/json; charset=utf-8"
        it.setHeader("Access-Control-Allow-Origin", "*")
        it.setHeader("Access-Control-Allow-Credentials", "true")
        it.setHeader("Server", "HyApiServer/2.1")
    }

    fun start() = jetty.start()
    fun stop() = jetty.stop()
    fun join() = jetty.join()
    fun startSync() = apply { start(); join() }
}
