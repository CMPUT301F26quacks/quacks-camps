package com.example.quackapp.Model

import kotlinx.serialization.Serializable

/**
 * Object used to pass campsiteIds between screens
 * @property campsiteIds campsite IDs passed to [com.example.quackapp.View.Owner.OwnerCreateLottery] to properly associate those camps to the same lottery
 */
@Serializable
data class CreateLotteryRoute(val campsiteIds: List<String>)