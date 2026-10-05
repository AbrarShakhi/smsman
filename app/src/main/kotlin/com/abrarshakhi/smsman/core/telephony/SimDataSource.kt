package com.abrarshakhi.smsman.core.telephony

import android.content.Context
import android.telephony.SubscriptionManager
import android.util.Log
import com.abrarshakhi.smsman.core.model.SimInfo

private const val TAG = "SimDataSource"

class SimDataSource(private val context: Context) {

    private val subscriptionManager: SubscriptionManager? =
        context.getSystemService(SubscriptionManager::class.java)

    fun activeSims(): List<SimInfo> = try {
        subscriptionManager?.activeSubscriptionInfoList.orEmpty().map { info ->
            SimInfo(
                subscriptionId = info.subscriptionId,
                slotIndex = info.simSlotIndex,
                displayName = info.displayName?.toString().orEmpty(),
                carrierName = info.carrierName?.toString().orEmpty(),
            )
        }
    } catch (e: SecurityException) {
        Log.w(TAG, "READ_PHONE_STATE not granted", e)
        emptyList()
    } catch (e: Exception) {
        Log.e(TAG, "Could not read active subscriptions", e)
        emptyList()
    }

    fun find(subscriptionId: Int, sims: List<SimInfo> = activeSims()): SimInfo? =
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            null
        } else {
            sims.firstOrNull { it.subscriptionId == subscriptionId }
        }
}
