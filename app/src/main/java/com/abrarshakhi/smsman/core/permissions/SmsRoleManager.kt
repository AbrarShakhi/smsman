package com.abrarshakhi.smsman.core.permissions

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent

/**
 * Wraps the SMS role. [RoleManager] is API 29+, so it covers the whole minSdk-30 range.
 *
 * Deliberately not using `Telephony.Sms.getDefaultSmsPackage()`: it additionally requires a
 * `<queries>` entry for SMS_DELIVER on API 30+, and the role is what the platform actually
 * consults. (Confirmed on device: `cmd role get-role-holders` reports the holder while
 * `settings get secure sms_default_application` is null.)
 */
class SmsRoleManager(context: Context) {

    private val roleManager: RoleManager? = context.getSystemService(RoleManager::class.java)

    fun isRoleAvailable(): Boolean = roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true

    fun isDefaultSmsApp(): Boolean = roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true

    /** Launch with `StartActivityForResult`; RESULT_OK means the role was granted. */
    fun requestRoleIntent(): Intent? = roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
}
