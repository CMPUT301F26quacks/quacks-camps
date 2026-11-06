package com.example.quackapp

import org.junit.Assert.assertEquals
import org.junit.Test

class LotteryDrawerTest {

    private fun newListing() = LotteryListing(
        lotteryId = "lottery1",
        campsiteId = "camp1",
        ownerId = "owner1",
        registrationStartDate = "2026-11-01",
        registrationEndDate = "2026-11-10",
        bookingStartDate = "2026-12-01",
        bookingEndDate = "2026-12-20",
        waitListCapacity = 10,
        status = "Open",
        maxWinners = 2
    )

    private fun addFiveEntrants(waitRepo: WaitlistRepository) {
        for (name in listOf("a", "b", "c", "d", "e")) {
            waitRepo.addEntry(WaitlistEntry("lottery1_$name", "lottery1", name))
        }
    }

    private fun entriesOf(waitRepo: WaitlistRepository): List<WaitlistEntry> {
        var result = emptyList<WaitlistEntry>()
        waitRepo.getEntriesForLottery("lottery1") { result = it }
        return result
    }


    @Test
    fun drawPicksTheRightNumberOfWinners() {
        val waitRepo = WaitlistRepository()
        val drawer = LotteryDrawer(waitRepo, ListingRepository())
        addFiveEntrants(waitRepo)

        drawer.runDrawAndSave(newListing()) { }

        val entries = entriesOf(waitRepo)
        assertEquals(2, entries.count { it.status == WaitlistStatus.SELECTED })
        assertEquals(3, entries.count { it.status == WaitlistStatus.NOT_SELECTED })
    }


}

