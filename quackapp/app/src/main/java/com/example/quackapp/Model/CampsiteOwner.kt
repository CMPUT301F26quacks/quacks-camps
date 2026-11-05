package com.example.quackapp.Model
import android.net.Uri
import com.example.quackapp.Model.User
import kotlin.random.Random

class CampsiteOwner(
    deviceId: String,
    name: String = "",
    email: String = "",
    phoneNumber: String = "",
    notificationsEnabled: Boolean = true,
    roles: List<String>,
    var ownedCampsites: MutableList<Campsite> = mutableListOf(),
    var ownedLotteries: MutableList<Lottery> = mutableListOf()
): User(deviceId, name, email, phoneNumber, notificationsEnabled, roles){
    //notification preference
    fun toggleNotificationPreference() {
        notificationsEnabled = !notificationsEnabled
    }
    //register campsite
    fun registerCampsite(campsite: Campsite): Campsite {
//        campsite.owner = this.name
        campsite.qrCode = generateQRCodeID(campsite)
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
    //function to generate the unique qr code id for a campsite
    fun generateQRCodeID(campsite: Campsite): String {
        val campsiteName = campsite.name
        val uniqueID = getRandomList(Random)
        return "campsite/${campsiteName}_${uniqueID.joinToString("")}"
    }

    fun generateQRCode(campsite: Campsite): String {
        //function to actually visually generate the qr code (api)
        return "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${Uri.encode(generateQRCodeID(campsite))}"    //uri.encode used so the string can be safely passed onto the url or something
    }

    //helper function to generate a random sequence of 5 nums for the qr code generator
    fun getRandomList(random: Random): List<Int> = List(5) {
        random.nextInt(0, 10)
    }


}