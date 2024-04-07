package org.amber.asparagus.fkec

class ApplicationState {
    companion object {
        var sessionInfo = ""
        var sessionKey: ByteArray = ByteArray(0)
    }
}