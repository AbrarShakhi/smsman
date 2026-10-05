package com.abrarshakhi.smsman.sms

import android.content.Context

fun permissionsAndRoleReady(context: Context, roleManager: SmsRoleManager): Boolean =
    roleManager.isDefaultSmsApp() && SmsPermissions.allGranted(context)
