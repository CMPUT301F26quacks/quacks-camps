package com.example.quackapp

class WaitlistRepository {

    val entries = mutableListOf<WaitlistEntry>()

    fun find(lotteryId: String, entrantId: String): WaitlistEntry? {
        return entries.find { it.lotteryId == lotteryId && it.entrantId == entrantId }
    }

    fun getEntriesForLottery(lotteryId: String, callback: (List<WaitlistEntry>) -> Unit) {
        callback(entries.filter { it.lotteryId == lotteryId })
    }

    fun addEntry(entry: WaitlistEntry) {
        entries.removeAll { it.entryId == entry.entryId }   // replace if it already exists
        entries.add(entry)
    }

    fun countWaiting(lotteryId: String): Int {
        return entries.count { it.lotteryId == lotteryId && it.isWaitListed() }
    }

    fun enter(listing: LotteryListing, entrantId: String): String {
        if (!listing.isRegistrationOpen()) {
            return "Registration is not open."
        }
        if (listing.isWaitlistFull(countWaiting(listing.lotteryId))) {
            return "The waiting list is full."
        }

        val existing = find(listing.lotteryId, entrantId)
        if (existing == null) {
            val entry = WaitlistEntry("${listing.lotteryId}_$entrantId",
                listing.lotteryId, entrantId)
            if (!entry.isValid()) {
                return "Invalid entry."
            }
            entries.add(entry)
            return "OK"
        }
        if (existing.status == WaitlistStatus.CANCELLED) {
            existing.status = WaitlistStatus.WAITING
            return "OK"
        }
        return "You already entered."
    }

    fun leave(listing: LotteryListing, entrantId: String): String {
        if (!listing.isRegistrationOpen()) return "Registration is closed."

        val existing = find(listing.lotteryId, entrantId)
        if (existing == null || !existing.isWaitListed()) return "You are not in this lottery."

        existing.cancelEntry()
        return "OK"
    }

    fun accept(listing: LotteryListing, entrantId: String): String {
        val entry = find(listing.lotteryId, entrantId)
        if (entry == null || !entry.isSelected()) return "You have no spot to accept."

        entry.status = WaitlistStatus.ACCEPTED
        return "OK"
    }
}