package com.example.quackapp.Controller

import android.util.Log
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.CampsiteSummary
import com.example.quackapp.Model.Lottery
import com.example.quackapp.Model.LotteryOffer
import com.example.quackapp.Model.LotteryStatus
import com.example.quackapp.Model.NotificationRepository
import com.example.quackapp.View.toDisplayDate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * Draws the lottery winners automatically after the registration period ends.
 * US 02.05.01 - As a campsite owner, I want the system to automatically draw winners after the registration period ends.
 */
object LotteryDraw {

    /**
     * Randomly picks the winners from the waitlist.
     *
     * @param waitlist device ids of the entrants in the waitlist
     * @param numWinners the number of winners the owner set
     * @param random only passed in for the tests so the result is the same every time
     * @return list of the winners device ids
     */
    fun pickWinners(waitlist: List<String>, numWinners: Int, random: Random = Random): List<String> {
        // get rid of empty ids and people who are in there twice
        val entrants = waitlist.filter { it != "" }.distinct()
        if (numWinners <= 0) {
            return listOf()
        }
        return entrants.shuffled(random).take(numWinners)
    }


    /**
     * Goes through all the open lotteries and draws the ones where registration is over.
     * Winners get a lottery offer + a win notification, everyone else gets a loss notification.
     *
     * NOTE: we dont have a server so this only runs when someone opens the app,
     * so the lottery gets drawn the next time the app is opened after registration ends
     *
     * @param db the firestore database
     * @param now current time in millis
     */
    suspend fun drawDueLotteries(db: FirebaseFirestore = FirebaseFirestore.getInstance(), now: Long = System.currentTimeMillis()) {
        try {
            val openLotteries = db.collection("lotteries")
                .whereEqualTo("status", LotteryStatus.OPEN.name)
                .get()
                .await()

            for (doc in openLotteries.documents) {
                val registrationEnd = doc.getLong("registrationPeriodEnd")
                // skip if theres no end date or registration is still going
                if (registrationEnd == null || registrationEnd > now) {
                    continue
                }

                // close the lottery first in a transaction so if 2 people open the app
                // at the same time it doesnt get drawn twice
                val closedIt = db.runTransaction { transaction ->
                    val snapshot = transaction.get(doc.reference)
                    if (snapshot.getString("status") == LotteryStatus.OPEN.name) {
                        transaction.update(doc.reference, "status", LotteryStatus.CLOSED.name)
                        true
                    } else {
                        false
                    }
                }.await()

                val lottery = doc.toObject(Lottery::class.java)
                if (closedIt && lottery != null) {
                    draw(db, lottery)
                }
            }
        } catch (e: Exception) {
            Log.w("LotteryDraw", "Couldn't draw the lotteries", e)
        }
    }


    /**
     * Picks the winners for one lottery, saves their offers and sends out the notifications.
     *
     * @param db the firestore database
     * @param lottery the lottery being drawn
     */
    private suspend fun draw(db: FirebaseFirestore, lottery: Lottery) {
        // get the campsites in this lottery
        val campsites = mutableListOf<Campsite>()
        for (campId in lottery.campIds) {
            if (campId == "") continue
            val campsite = db.collection("campsites").document(campId).get().await().toObject(Campsite::class.java)
            if (campsite != null) {
                campsites.add(campsite)
            }
        }

        var title = campsites.joinToString(" & ") { it.name }
        if (title == "") {
            title = "Campsite lottery"
        }

        // the booking period is the date range winners can pick
        val dates = mutableListOf<String>()
        if (lottery.bookingPeriodStart != null && lottery.bookingPeriodEnd != null) {
            dates.add(lottery.bookingPeriodStart!!.toDisplayDate() + " - " + lottery.bookingPeriodEnd!!.toDisplayDate())
        }

        val summaries = campsites.map { camp ->
            CampsiteSummary(
                campsiteId = camp.campId,
                name = camp.name,
                location = camp.location,
                pricePerNight = camp.price.toDouble(),
                maxOccupancy = camp.capacity,
                description = camp.features.joinToString(", ")
            )
        }

        val winners = pickWinners(lottery.waitlist, lottery.numLotteryWinners)
        val notifications = NotificationRepository.getInstance()

        // winners get an offer so it shows up in their lottery wins tab
        for (entrantId in winners) {
            val offer = LotteryOffer(
                lotteryTitle = title,
                entrantDeviceId = entrantId,
                campsites = summaries,
                availableDateRanges = dates
            )
            db.collection("lottery_offers").document(offer.id).set(offer).await()
            notifications.notifyLotteryWin(entrantId, title, offer.id)
        }

        // everyone else lost :(
        for (entrantId in lottery.waitlist.distinct()) {
            if (entrantId != "" && entrantId !in winners) {
                notifications.notifyLotteryLoss(entrantId, title)
            }
        }
    }
}
