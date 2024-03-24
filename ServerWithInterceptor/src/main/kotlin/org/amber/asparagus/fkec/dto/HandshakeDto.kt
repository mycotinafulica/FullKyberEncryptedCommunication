package org.amber.asparagus.fkec.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class HandshakeInput(
    @JsonProperty("encapsulated_secret")
    val encapsulatedSecret: String
)

data class HandshakeOutput(
    @JsonProperty("status")
    val status: String,
    @JsonProperty("session_info")
    val sessionInfo: String
)