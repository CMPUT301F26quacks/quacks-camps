package com.example.quackapp.Controller

import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import kotlin.collections.filter

/**
 * Returns Campsites that meet the entrants filters
 *
 * Go through each campsite and check if it meets the filter requirements
 */
class FilterCampsiteController {
    /**
     * @param query the input in the search box
     * @param capacityRange the selected capacity range in the filter
     * @param searchByLocation if Search By Location box is checked, true, else false
     * @param campsites the current list of all campsites in firebase
     * @param lotteries the current list of all lotteries in firebase
     * @param inputtedStartingDate the inputted start date, else null
     * @param inputtedEndingDate the inputted end date, else null
     */
    fun filterCampsites(
        query: String,
        capacityRange: ClosedFloatingPointRange<Float>,
        searchByLocation: Boolean,
        campsites: List<Campsite>,
        lotteries: List<Lottery>,
        inputtedStartingDate: Long?,
        inputtedEndingDate: Long?
    ): List<Campsite> {
        //Returns only the campsites that meet the criteria
        return campsites.filter { campsite ->
            var testsPassed = 0

            //1. Check if campsite passes capacity filter settings
            if (campsite.capacity.toFloat() in capacityRange) {
                testsPassed += 1
            }

            //2. Check if campsite passes date range filter
            val datesMatch = inputtedStartingDate == null || inputtedEndingDate == null ||
                lotteries.any { lottery ->

                    val start = campsite.availability.minOrNull()
                    val end = campsite.availability.maxOrNull()
                    //Checks to see if the campsites availability is between the filtered range
                    lottery.lotteryID == campsite.lotteryId &&
                            start != null && end != null &&
                            start <= inputtedStartingDate &&
                            end >= inputtedEndingDate
                }
            if (datesMatch) {
                    testsPassed += 1
            }


            //3. Check if campsite aligns with search box query
            if (query.isBlank()) {
                testsPassed += 1
            }
            //If we are searching by name, check if query aligns with campsite name
            else if (!searchByLocation && campsite.name.contains(query, ignoreCase = true)) {
                testsPassed += 1
            }
            //If we are searching by location, check if query aligns with campsite location
            else if (searchByLocation && campsite.location.contains(query, ignoreCase = true)) {
                testsPassed += 1
            }

            //If all tests are passed, return true, else false
            if(testsPassed == 3){
                return@filter true
            }
            else{
                return@filter false
            }


        }

    }
}