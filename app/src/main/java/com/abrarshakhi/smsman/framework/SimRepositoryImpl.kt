package com.abrarshakhi.smsman.framework

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.abrarshakhi.smsman.domain.model.Sim
import com.abrarshakhi.smsman.domain.repository.SimRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SimRepository {

    override fun listSims(): List<Sim> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) return emptyList()

        val manager = context.getSystemService(SubscriptionManager::class.java) ?: return emptyList()
        return manager.activeSubscriptionInfoList?.map { info ->
            Sim(
                subscriptionId = info.subscriptionId,
                carrierName = info.carrierName?.toString().orEmpty(),
                displayName = info.displayName?.toString()
                    ?: "SIM ${info.simSlotIndex + 1}",
                phoneNumber = info.number?.takeIf { it.isNotBlank() },
                slotIndex = info.simSlotIndex,
                iconTint = info.iconTint.toLong(),
            )
        } ?: emptyList()
    }
}
