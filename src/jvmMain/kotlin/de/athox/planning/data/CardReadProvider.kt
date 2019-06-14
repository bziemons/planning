package de.athox.planning.data

import CardState

interface CardReadProvider {
    fun allCards(): Iterable<Int>

    @Throws(CardNotFoundException::class)
    fun getCard(cardId: Int): CardState

}
