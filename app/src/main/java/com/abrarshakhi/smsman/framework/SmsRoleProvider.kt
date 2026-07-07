package com.abrarshakhi.smsman.framework

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Android's default-SMS-app role and runtime-permission state.
 *
 * Default-app status is queried via [Telephony.Sms.getDefaultSmsPackage]. Role requests use the
 * [RoleManager] API (available since Q / API 29; min SDK 30 so always present).
 */
@Singleton
class SmsRoleProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isDefaultSmsApp(): Boolean =
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

    fun createRoleRequestIntent(): Intent {
        val roleManager = context.getSystemService(RoleManager::class.java)
        return roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
    }

    /**
     * Runtime permissions we ask for during onboarding (in addition to the default-SMS-app role).
     * The SMS-group permissions (SEND/RECEIVE/READ) are auto-granted to the default SMS app, so we
     * don't include them here.
     */
    fun requiredRuntimePermissions(): List<String> {
        val base = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            base.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return base
    }

    fun missingRuntimePermissions(): List<String> =
        requiredRuntimePermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
}
