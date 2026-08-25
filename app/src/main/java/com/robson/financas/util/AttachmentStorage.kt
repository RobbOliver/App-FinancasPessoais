package com.robson.financas.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object AttachmentStorage {
    fun copyToInternalStorage(context: Context, uri: Uri): String? {
        val dir = File(context.filesDir, "attachments").apply { mkdirs() }
        val destination = File(dir, "${UUID.randomUUID()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            }
            destination.absolutePath
        } catch (e: java.io.IOException) {
            null
        }
    }
}
