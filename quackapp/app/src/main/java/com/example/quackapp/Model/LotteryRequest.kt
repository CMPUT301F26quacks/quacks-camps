package com.example.quackapp.Model

/**
 * Request used between campsite owners
 * when owner A wants to enter a camp belonging to owner B
 * into a lottery with A's camps
 * @property requestId unique ID to identify specific requests
 * @property lotteryId unique ID to identify the lottery associated with this request
 * @property campId unique ID to identify the camp the requester wants to put in a lottery
 * @property campName name of the campsite the requesters want in their lottery
 * @property requesterId unique ID of the owner making the lottery
 * @property ownerId unique ID of the owner whose camp is being requested
 * @property status status of the request (PENDING, APPROVED, or DECLINED)
 * @property createdAt timestamp in milliseconds of when the request was made
 */
data class LotteryRequest(
    val requestId: String = "",
    val lotteryId: String = "",
    val campId: String = "",
    val campName: String = "",
    val requesterId: String = "",
    val ownerId: String = "",
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)