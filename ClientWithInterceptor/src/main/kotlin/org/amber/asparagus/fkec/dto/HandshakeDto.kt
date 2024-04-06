package org.amber.asparagus.fkec.dto

import com.google.gson.annotations.SerializedName

data class HandshakeInput(
    @SerializedName("encapsulated_secret")
    val encapsulatedSecret: String
)

data class HandshakeOutput(
    @SerializedName("status")
    val status: String,
    @SerializedName("session_info")
    val sessionInfo: String
)