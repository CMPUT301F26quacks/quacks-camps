package com.example.quackapp.Model

import kotlinx.serialization.Serializable

/**
 * Object used to pass campsiteIds between screens
 */
@Serializable
data class ListCampInLotteryRoute(val campsiteIds: List<String>)