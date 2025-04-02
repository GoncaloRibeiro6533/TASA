package pt.isel

/**
 * Represents a contact exclusion to violate the silent mode.
 * @property id the contact exclusion's id
 * @property name the contact exclusion's name
 * @property phoneNumber the contact exclusion's phone number
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
class ContactExclusion(
    id: Int,
    val name: String,
    val phoneNumber: String,
) : Exclusion(id) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(name.length <= MAX_NAME_LENGTH) {
            "name must not be longer than $MAX_NAME_LENGTH"
        }
        require(phoneNumber.isNotBlank()) { "phoneNumber must not be blank" }
        require(phoneNumber.length <= MAX_PHONE_NUMBER_LENGTH) {
            "phoneNumber must not be longer than $MAX_PHONE_NUMBER_LENGTH"
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_PHONE_NUMBER_LENGTH = 15 //  https://en.wikipedia.org/wiki/E.164#cite_note-:0-1
    }
}
