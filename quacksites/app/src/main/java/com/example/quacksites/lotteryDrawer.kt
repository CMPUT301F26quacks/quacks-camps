package com.example.quacksites


class lotteryDrawer {
    private val waitlistRepository = waitlistRepository()

    fun runDrawAndSave(lotteryId: String, maxWinners: Int, onComplete: (List<waitListEntry>) -> Unit) {
        waitlistRepository.getEntriesForLottery(lotteryId) { entries ->
            val waitlistedEntrants = entries.filter { it.status == waitListStatus.WAITING }
            val shuffled = waitlistedEntrants.shuffled()

            for (i in shuffled.indices) {
                if (i < maxWinners) {
                    shuffled[i].status = waitListStatus.SELECTED
                } else {
                    shuffled[i].status = waitListStatus.NOT_SELECTED
                }
                    // Push updated status back to Firebase
                waitlistRepository.addEntry(shuffled[i])
            }
            onComplete(shuffled)
        }
    }
}