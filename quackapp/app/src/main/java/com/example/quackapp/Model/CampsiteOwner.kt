package com.example.quackapp.model
import com.example.quackapp.Model.User

class CampsiteOwner(
    deviceId: String,
    name: String = "",
    email: String = "",
    phoneNumber: String = "",
    notificationsEnabled: Boolean = true,
    var ownedCampsites: MutableList<Campsite> = mutableListOf(),
    var ownedLotteries: MutableList<Lottery> = mutableListOf()
): User(deviceId, name, email, phoneNumber, notificationsEnabled){
    //notification preference
    fun toggleNotificationPreference() {
        notificationsEnabled = !notificationsEnabled
    }
    //register campsite
    fun registerCampsite(campsite: Campsite): Campsite {
        campsite.owner = this.name
        campsite.qrCode = generateQRCode(campsite.name)
        ownedCampsites.add(campsite)
        return campsite
    }

    //update campsite
    fun updateCampsite(campsite: Campsite): Campsite {
        val index = ownedCampsites.indexOfFirst { it.name == campsite.name }    //find og campsite by matching name
        if(index != -1){
            ownedCampsites[index] = campsite
        }
        return campsite
    }

    //delete campsite
    fun deleteCampsite(campsite: Campsite) {
        ownedCampsites.remove(campsite)
        //tho it js creates the assumption/general thing that if ur deleting the campsite and it has a lottery active, you need to delete the lottery first
    }

    //register/set lottery
    fun registerLottery(campsite: Campsite, lottery: Lottery): Lottery {
        lottery.status = LotteryStatus.OPEN
        campsite.lottery = lottery
        ownedLotteries.add(lottery)
        return lottery
    }

    //update lottery
    fun updateLottery(campsite: Campsite,updatedLottery: Lottery): Lottery {
        //update status to active
        campsite.lottery = updatedLottery
        //update
        val index = ownedLotteries.indexOfFirst { it.lotteryID == updatedLottery.lotteryID }
        if(index != -1){
            ownedLotteries[index] = updatedLottery
        }
        return updatedLottery
    }

    //remove lottery status (closing it)
    fun removeLotteryStatus(campsite: Campsite, lottery: Lottery) {
        campsite.lottery = null
        ownedLotteries.remove(lottery)
        lottery.status = LotteryStatus.CLOSED
    }

    //generate qr code - user story
    fun generateQRCode(campsiteName: String): String {

    }


}