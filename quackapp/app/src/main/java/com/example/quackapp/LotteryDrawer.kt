package com.example.quackapp

class DrawMessage(
    val entrantId: String,
    val message: String
)
class LotteryDrawer(
    private val WaitlistRepo: WaitlistRepository,
    private val ListingRepo: ListingRepository
) {

    private val notifications = mutableListOf<DrawMessage>()

    // draw
    fun runDrawAndSave(listing: LotteryListing, onComplete: (List<WaitlistEntry>) -> Unit) {
        if (listing.status.equals("Drawn", true)) {
            onComplete(emptyList())
            return
        }
        WaitlistRepo.getEntriesForLottery(listing.lotteryId) { entries ->
            val shuffled = entries
                .filter { it.status == WaitlistStatus.WAITING }
                .shuffled()

            for ((i, entry) in shuffled.withIndex()) {
                if (i < listing.maxWinners) {
                    entry.status = WaitlistStatus.SELECTED
                    notifications.add(DrawMessage(entry.entrantId, "You won the Lottery!"))
                } else {
                    entry.status = WaitlistStatus.NOT_SELECTED
                    notifications.add(
                        DrawMessage(
                            entry.entrantId,
                            "You were not selected in the Lottery."
                        )
                    )
                }
                WaitlistRepo.addEntry(entry)
            }
            listing.status = "Drawn"
            ListingRepo.addListing(listing, listing.ownerId)

            onComplete(shuffled)
        }
    }

}