package com.example.quackapp.Controller

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ownerHomeController{
    private val db = Firebase.firestore
    private val _campsitesRef = db.collection("campsites")


}