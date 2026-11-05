package com.example.quackapp

class ListingRepository {

    private val listings = mutableListOf<LotteryListing>()

    fun addListing(listing: LotteryListing, ownerId: String): String {
        val result = listing.ValidateListing()
        if (!result.isValid) return result.errorMessage

        if (listing.ownerId != ownerId) {
            return "Only the owner can create a lottery."
        }
        if (getListingThroughId(listing.lotteryId) != null) {
            return "That lottery ID already exists."
        }

        listings.add(listing)
        return "OK"
    }

    fun getAllListings(): List<LotteryListing> {
        return listings.toList()
    }

    fun getListingThroughId(lotteryId: String): LotteryListing? {
        return listings.find { it.lotteryId == lotteryId }
    }

    fun getListingsByOwner(ownerId: String): List<LotteryListing> {
        return listings.filter { it.ownerId == ownerId }
    }

    fun removeListing(lotteryId: String): Boolean {
        return listings.removeAll { it.lotteryId == lotteryId }
    }

    fun updateListing(updated: LotteryListing, ownerId: String): String {
        val result = updated.ValidateListing()
        if (!result.isValid) return result.errorMessage

        val index = listings.indexOfFirst { it.lotteryId == updated.lotteryId }
        if (index == -1){
            return "Lottery not found."
        }
        if (listings[index].ownerId != ownerId) {
            return "Only the owner can edit this lottery."
        }
        if (listings[index].status.equals("Drawn", true)){
            return "You can't edit a lottery after the draw."
        }

        listings[index] = updated
        return "OK"
    }
}