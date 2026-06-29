package com.abrarshakhi.smsman.domain.repository

import java.io.InputStream
import java.io.OutputStream

interface BackupRepository {
    suspend fun export(outputStream: OutputStream): Int
    suspend fun import(inputStream: InputStream): Int
}
