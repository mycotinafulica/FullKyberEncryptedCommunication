package org.amber.asparagus.fkec.dto

import com.google.gson.annotations.SerializedName

/*
* I/O for basic primitive types
* */
data class TransactionRequest (
    @SerializedName("field1")
    val field1: String,
    @SerializedName("field2")
    val field2: Int,
    @SerializedName("field3")
    val field3: Boolean
)

data class TransactionResponse (
    @SerializedName("reps1")
    val reps1: String,
    @SerializedName("resp2")
    val resp2: Int,
    @SerializedName("resp3")
    val resp3: Boolean
)

/*
* Complex I/O with nested object
* */
data class ComplexTransactionRequest (
    @SerializedName("field1")
    val field1: String,
    @SerializedName("field2")
    val field2: Int,
    @SerializedName("field3")
    val field3: Boolean,
    @SerializedName("field4")
    val field4: TransactionRequest,
    @SerializedName("field5")
    val field5: TransactionRequest
)

data class ComplexTransactionResponse (
    @SerializedName("reps1")
    val reps1: String,
    @SerializedName("resp2")
    val resp2: Int,
    @SerializedName("resp3")
    val resp3: Boolean,
    @SerializedName("resp4")
    val resp4: TransactionResponse,
    @SerializedName("resp5")
    val resp5: TransactionResponse
)