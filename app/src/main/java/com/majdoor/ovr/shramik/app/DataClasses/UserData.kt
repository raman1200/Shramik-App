package com.majdoor.ovr.shramik.app.DataClasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    var uid: String? = null,
    var name: String? = null,
    var email: String? = null,
    var address: String? = null,
    var number: String? = null,
    var pincode: String? = null,
    var state: String? = null,
    var district: String? = null,
    var gender: String? = null,
    var appliedFor: String? = null,
    var image: String? = null,
    var experience: String? = null
) : Parcelable