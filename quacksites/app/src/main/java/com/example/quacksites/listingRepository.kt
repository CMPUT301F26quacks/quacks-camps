package com.example.quacksites
//firebase
class listingRepository {
    private val listings = mutableListOf<lotteryListing>()

    //adds a listing, checks if valid using the restrictions on lotteryListing
    fun addListing(listing: lotteryListing): Boolean{
        if(listing.validateListing().isValid){
            listings.add(listing) //adds if valid
            return true
        }
        return false //if not valid
    }

    // gets all listings into a list currently saved in memory
    fun getAllListings(): List<lotteryListing> = listings.toList()

    //searches through the stored list using the Id
    fun getListingThroughId(lotteryId: String): lotteryListing?{
        return listings.find {it.lotteryId == lotteryId}
    }

    //searches through the stored list using a specific Owner Name
    fun getListingsByOwner(ownerId: String): List<lotteryListing> {
        return listings.filter { it.ownerId == ownerId }
    }

    // searches for the listing and removes it from the mutable list
    fun removeListing(lotteryId: String): Boolean {
        return listings.removeIf { it.lotteryId == lotteryId }
    }
}