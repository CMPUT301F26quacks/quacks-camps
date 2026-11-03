package com.example.quacksites


class lotteryDrawer {
    private val waitlistRepository = waitlistRepository()

    fun runDrawAndSave(lotteryId: String, maxWinners: Int, onComplete: (List<waitListEntry>) -> Unit) {
        waitlistRepository.getEntriesForLottery(lotteryId) { entries ->
            val waitlistedEntrants = entries.filter { it.status.equals("Waitlisted", ignoreCase = true) }
            val shuffled = waitlistedEntrants.shuffled()

            for (i in shuffled.indices) {
                if (i < maxWinners) {
                    shuffled[i].status = "Selected"
                } else {
                    shuffled[i].status = "Not_Selected"
                }
                    // Push updated status back to Firebase
                waitlistRepository.addEntry(shuffled[i])
            }
            onComplete(shuffled)
        }
    }
}