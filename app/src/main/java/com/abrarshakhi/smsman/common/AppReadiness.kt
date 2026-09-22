package com.abrarshakhi.smsman.common

import android.content.Context
import com.abrarshakhi.smsman.core.permissions.SmsPermissions
import com.abrarshakhi.smsman.core.permissions.SmsRoleManager

/** The app can only read or store messages once it holds the SMS role *and* the runtime grants. */
fun permissionsAndRoleReady(context: Context, roleManager: SmsRoleManager): Boolean =
    roleManager.isDefaultSmsApp() && SmsPermissions.allGranted(context)
