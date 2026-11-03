package com.example.quacksites
//firebase and change it later
class waitlistRepository {

    private val collection = db.collection("waitlist")

    fun addEntry(entry: waitListEntry): Boolean{
        if(entry.validateListing().isValid){
            entries.add(entry) //adds if valid
            return true
        }
        return false //if not valid
    }

    fun getEntriesForLottery(
        lotteryId: String,
        onResult: (List<waitListEntry) ->){

    }
}