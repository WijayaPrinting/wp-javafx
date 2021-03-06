package com.hendraanggrian.openpss.route

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.Server
import com.hendraanggrian.openpss.nosql.transaction
import com.hendraanggrian.openpss.schema.Invoices
import com.hendraanggrian.openpss.schema.Log
import com.hendraanggrian.openpss.schema.Logs
import com.hendraanggrian.openpss.schema.Payment
import com.hendraanggrian.openpss.schema.Payments
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.nosql.equal

fun Routing.payment() {
    route(Payments.schemaName) {
        get {
            call.respond(transaction {
                Payments { dateTime.matches(call.parameters["dateTime"]!!) }.toList()
            })
        }
        post {
            val payment = call.receive<Payment>()
            payment.id = transaction { Payments.insert(payment) }
            call.respond(payment)
        }
        delete {
            val payment = call.receive<Payment>()
            transaction {
                val invoiceNo = Invoices[payment.invoiceId].single().no
                Payments -= payment.id
                Logs += Log.new(
                    Server.getString(R.string.payment_delete).format(
                        payment.value,
                        invoiceNo
                    ),
                    call.getString("login")
                )
            }
            call.respond(HttpStatusCode.OK)
        }
        route("{invoiceId}") {
            get {
                call.respond(transaction {
                    Payments { invoiceId.equal(call.getString("invoiceId")) }.toList()
                })
            }
            get("due") {
                call.respond(transaction {
                    Payments { invoiceId.equal(call.getString("invoiceId")) }
                        .sumByDouble { it.value }
                })
            }
        }
    }
}
