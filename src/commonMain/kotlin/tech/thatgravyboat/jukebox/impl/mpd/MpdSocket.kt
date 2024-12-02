package tech.thatgravyboat.jukebox.impl.mpd

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

internal class MpdSocket(private val port: Int) {

    private var manager: SelectorManager? = null
    private var socket: Socket? = null
    private var read: ByteReadChannel? = null
    private var write: ByteWriteChannel? = null

    fun start() {
        runBlocking {
            manager = SelectorManager(Dispatchers.IO)
            socket = aSocket(SelectorManager(Dispatchers.IO)).tcp().connect("localhost", port)
            read = socket!!.openReadChannel()
            write = socket!!.openWriteChannel(true)

            read!!.readUTF8Line() // Reads the version number
        }
    }

    fun close(): Boolean = runCatching {
        socket?.close()
        manager?.close()

        manager = null
        socket = null
        read = null
        write = null
    }.isSuccess

    fun isOpen() = socket?.isClosed == false && manager != null && read != null && write != null

    suspend fun send(message: String, times: Int = 5): Result<String> {
        if (!this.isOpen()) {
            if (times <= 0) return Result.failure(Error("Failed to reconnect to service."))
            start()
            return send(message, times - 1)
        } else {
            val responses = mutableListOf<String>()

            write!!.writeStringUtf8("$message\n")

            while (responses.lastOrNull() != "OK" && responses.lastOrNull()?.startsWith("ACK") != true) {
                val response = read!!.readUTF8Line()
                if (response == null) {
                    this.close()
                    return send(message, times)
                } else {
                    responses.add(response)
                }
            }

            val last = responses.last()

            return when {
                last == "OK" -> Result.success(responses.dropLast(1).joinToString("\n"))
                last.startsWith("ACK") -> Result.failure(Error("Error when sending command: ${last.removePrefix("ACK ")}"))
                else -> Result.failure(Error("Response was not ended properly, connection may have been closed."))
            }
        }
    }

}