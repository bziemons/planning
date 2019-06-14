package de.athox.planning.data

import CardState

interface CardWriteProvider {
    fun insertCard(): Int

    fun updateCard(cardId: Int, obj: CardState)

}
