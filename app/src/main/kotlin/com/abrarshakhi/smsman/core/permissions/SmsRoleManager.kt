package com.abrarshakhi.smsman.core.permissions

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent

class SmsRoleManager(context: Context) {

    private val roleManager: RoleManager? = context.getSystemService(RoleManager::class.java)

    @Suppress("unused")
    fun isRoleAvailable(): Boolean = roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true

    fun isDefaultSmsApp(): Boolean = roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true

    fun requestRoleIntent(): Intent? = roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
}
