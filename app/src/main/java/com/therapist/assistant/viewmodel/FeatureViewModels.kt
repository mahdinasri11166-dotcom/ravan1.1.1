package com.therapist.assistant.viewmodel

/**
 * اسکلت معماری — بدون وابستگی domain تا بیلد پایدار بماند.
 * منطق واقعی در AppointmentViewModel است.
 */
class ClientsLogic

class FinanceLogic

class ReminderLogic

object SecretaryGate {
    fun isSecretary(roleName: String?): Boolean =
        roleName != null && roleName.equals("SECRETARY", ignoreCase = true)
}
