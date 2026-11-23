package com.example.quackapp.Controller

import androidx.compose.runtime.remember
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import kotlin.collections.filter

class FilterCampsiteController {

    fun filterCampsites(
        query: String,
        capacityRange: ClosedFloatingPointRange<Float>,
        searchByLocation: Boolean,
        campsites: List<Campsite>,
        lotteries: List<Lottery>,
        startingDate: Long?,
        endingDate: Long?
    ): List<Campsite> {
        return campsites.filter { campsite ->
            var testsPassed = 0

            //1. Check if campsite meets capacity requirement
            if (campsite.capacity.toFloat() in capacityRange) {
                testsPassed += 1
            }

            //2. Check if campsites meets date range requirement
            val datesMatch = startingDate == null || endingDate == null ||
                lotteries.any { lottery ->
                    val start = lottery.registrationPeriodStart?.toDate()?.time
                    val end = lottery.registrationPeriodEnd?.toDate()?.time

                    lottery.lotteryID == campsite.lotteryId &&
                            start != null && end != null &&
                            start >= startingDate &&
                            end <= endingDate
                }
            if (datesMatch) {
                    testsPassed += 1
            }


            //3. Check if campsite meets search box requirements
            if (query.isBlank()) {
                testsPassed += 1
            } else if (!searchByLocation && campsite.name.contains(query, ignoreCase = true)) {
                testsPassed += 1
            } else if (searchByLocation && campsite.location.contains(query, ignoreCase = true)) {
                testsPassed += 1
            }

            if(testsPassed == 3){
                return@filter true
            }
            else{
                return@filter false
            }


        }

    }
}