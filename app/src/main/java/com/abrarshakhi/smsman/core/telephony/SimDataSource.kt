package com.abrarshakhi.smsman.core.telephony

import android.content.Context
import android.telephony.SubscriptionManager
import android.util.Log
import com.abrarshakhi.smsman.core.model.SimInfo

private const val TAG = "SimDataSource"

class SimDataSource(private val context: Context) {

    private val subscriptionManager: SubscriptionManager? =
        context.getSystemService(SubscriptionManager::class.java)

    /**
     * Requires READ_PHONE_STATE. The list is **@Nullable until SDK 35**, so the null branch is
     * reachable on this API 31 device despite what the current docs say, and the call throws if the
     * permission is missing.
     *
     * Already sorted by slot index by the platform, so that ordering is relied on rather than
     * re-sorted here.
     */
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

    /**
     * Null for a subscription that is no longer active — historical messages legitimately reference
     * removed SIMs (the test device still lists an inactive subscription with slotIndex -1), so
     * callers must render a neutral fallback rather than treating this as an error.
     */
    fun find(subscriptionId: Int, sims: List<SimInfo> = activeSims()): SimInfo? =
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            null
        } else {
            sims.firstOrNull { it.subscriptionId == subscriptionId }
        }
}
