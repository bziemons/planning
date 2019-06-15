package de.athox.planning.data

import CardState
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class SQLiteBackend : CardReadProvider, CardWriteProvider {
    companion object {
        private const val DB_FILE = "planning.db"
    }

    private object Cards : IntIdTable() {
        val title = text("title")
        val body = text("body")
    }

    private var db: Database = Database.connect("jdbc:sqlite:$DB_FILE", driver = "org.sqlite.JDBC")

    init {
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE

        transaction(db) {
            SchemaUtils.create(Cards)
        }
    }

    override fun insertCard(): Int {
        return transaction(db) {
            Cards.insertAndGetId {
                it[title] = ""
                it[body] = ""
            }
        }.value
    }

    override fun updateCard(cardId: Int, obj: CardState) {
        transaction(db) {
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
        val result = transaction(db) {
            Cards.select { Cards.id.eq(cardId) }.firstOrNull()
        } ?: throw CardNotFoundException(cardId)

        return CardState(
            null,
            result[Cards.id].value,
            result[Cards.title],
            result[Cards.body]
        )
    }

    override suspend fun allCards(): List<Int> {
        return transaction(db) {
            Cards.selectAll().map { it[Cards.id].value }
        }
    }
}
