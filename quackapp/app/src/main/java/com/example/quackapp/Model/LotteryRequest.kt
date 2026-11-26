package com.example.quackapp.Model

/**
 * Request used between campsite owners
 * when owner A wants to enter a camp belonging to owner B
 * into a lottery with A's camps
 */
data class LotteryRequest(
    val requestId: String = "",
    val lotteryId: String = "",
    val campId: String = "",
    val campName: String = "",      // denormalized so the inbox needs no extra reads
    val requesterId: String = "",    // owner who made the lottery
    val ownerId: String = "",        // owner of the camp being requested
    val status: String = "PENDING", // PENDING, APPROVED, DECLINED
    val createdAt: Long = System.currentTimeMillis()
)