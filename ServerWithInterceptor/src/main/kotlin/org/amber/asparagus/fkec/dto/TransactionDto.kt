package org.amber.asparagus.fkec.dto

import com.fasterxml.jackson.annotation.JsonProperty

/*
* I/O for basic primitive types
* */
data class TransactionRequest (
    @JsonProperty("field1")
    val field1: String,
    @JsonProperty("field2")
    val field2: Int,
    @JsonProperty("field3")
    val field3: Boolean
)

data class TransactionResponse (
    @JsonProperty("reps1")
    val reps1: String,
    @JsonProperty("resp2")
    val resp2: Int,
    @JsonProperty("resp3")
    val resp3: Boolean
)

/*
* Complex I/O with nested object
* */
data class ComplexTransactionRequest (
    @JsonProperty("field1")
    val field1: String,
    @JsonProperty("field2")
    val field2: Int,
    @JsonProperty("field3")
    val field3: Boolean,
    @JsonProperty("field4")
    val field4: TransactionRequest,
    @JsonProperty("field5")
    val field5: TransactionRequest
)

data class ComplexTransactionResponse (
    @JsonProperty("reps1")
    val reps1: String,
    @JsonProperty("resp2")
    val resp2: Int,
    @JsonProperty("resp3")
    val resp3: Boolean,
    @JsonProperty("resp4")
    val resp4: TransactionResponse,
    @JsonProperty("resp5")
    val resp5: TransactionResponse
)