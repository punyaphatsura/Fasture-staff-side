package com.fasture.postman.application.repository.server

data class ParcelData (
    var destination:Destination,
    var receiver: Person,
    var sender: Person,
    var id:String,
    var preference: Preferences,
    var stamp_id:String,
    var status:Status,


)

data class Destination(
    var id:String,
    var owner:String?,
    var receiver: Person?,
    var receiverAddress: Address?,

)

data class Person(
    var name:String,
    var tel:String,
)

data class Address(
    var country:String?,
    var district:String?,
    var geoLocation:GeoLocation?,
    var houseNo:String?,
    var postalCode:String,
    var province:String?,
    var street:String?,
    var suburb:String?,
)

data class GeoLocation(
    var latitude:Float?,
    var longitude:Float?,
    var text:String?,

    )

data class Preferences(
    var deliveryTime:String?,
    var note:String?,

    )

data class Status(
    var deliveryStatus :DeliveryStatus,
    var paymentStatus:PaymentStatus,

)

data class DeliveryStatus(
    var logs:Array<Logs>?,
    var status:String?,
)

data class PaymentStatus(
    var price:Float?,
    var status:String?
)

data class Logs(
    var GeoLocation:GeoLocation?,
    var status:String?,
    var timestamp:String?
)