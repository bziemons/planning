package de.athox.planning.data

class CardNotFoundException : RuntimeException {
    constructor(cardId: Int) : super("Card with id $cardId not found")

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
