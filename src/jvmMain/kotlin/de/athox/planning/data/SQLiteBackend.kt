package de.athox.planning.data

import CardState
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class SQLiteBackend : CardReadProvider, CardWriteProvider {
    private object Cards : IntIdTable() {
        val title = text("title")
        val body = text("body")
    }

    init {
        Database.connect("jdbc:sqlite:planning.db", driver = "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE

        transaction {
            SchemaUtils.create(Cards)
        }
    }

    override fun insertCard(): Int {
        return transaction {
            Cards.insertAndGetId {
                it[title] = ""
                it[body] = ""
            }
        }.value
    }

    override fun updateCard(cardId: Int, obj: CardState) {
        transaction {
            Cards.update({ Cards.id.eq(cardId) }) {
                if (obj.title != null) {
                    it[title] = obj.title
                }
                if (obj.body != null) {
                    it[body] = obj.body
                }
            }
        }
    }

    @Throws(CardNotFoundException::class)
    override fun getCard(cardId: Int): CardState {
        val result = transaction {
            Cards.select { Cards.id.eq(cardId) }.firstOrNull()
        } ?: throw CardNotFoundException("Card with id $cardId not found")

        return CardState(
            null,
            result[Cards.id].value,
            result[Cards.title],
            result[Cards.body]
        )
    }

    override fun allCards(): Iterable<Int> {
        return transaction {
            Cards.selectAll().mapLazy { it[Cards.id].value }
        }
    }
}
