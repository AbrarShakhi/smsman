package com.abrarshakhi.smsman.common

import android.content.Context
import com.abrarshakhi.smsman.core.permissions.SmsPermissions
import com.abrarshakhi.smsman.core.permissions.SmsRoleManager

fun permissionsAndRoleReady(context: Context, roleManager: SmsRoleManager): Boolean =
    roleManager.isDefaultSmsApp() && SmsPermissions.allGranted(context)
