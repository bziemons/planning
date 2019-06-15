package de.athox.planning.data

import CardState
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.gson.gsonDeserializer

class GitHubBackend(private val owner: String, private val repo: String) : CardReadProvider {

    var issues: List<GitHubIssue> = emptyList()

    override suspend fun allCards(): List<Int> {
        val allIssues = Fuel.get("https://api.github.com/repos/$owner/$repo/issues")
            .header("Accept", "application/vnd.github.v3+json")
        val (request, response, result) = allIssues.awaitObjectResponseResult(
            gsonDeserializer<Array<GitHubIssue>>())
        issues = result.get().toMutableList()
        return issues.map { it.number }
    }

    @Throws(CardNotFoundException::class)
    override fun getCard(cardId: Int): CardState {
        return issues.find { it.number == cardId }?.let {
            CardState(null, it.number, it.title, it.body)
        } ?: throw CardNotFoundException(cardId)
    }

    class GitHubIssue(
        val id: Int,
        val node_id: String,
        val html_url: String,
        val number: Int,
        val state: String,
        val title: String,
        val body: String,
        val user: GitHubUser,
        val labels: Array<GitHubLabel>,
        val assignee: GitHubUser,
        val assignees: Array<GitHubUser>,
        val milestone: GitHubMilestone,
        val locked: Boolean,
        val active_lock_reason: String,
        val comments: Int,
        val pull_request: GitHubPRRef,
        val closed_at: String?,
        val created_at: String,
        val updated_at: String
    )

    class GitHubPRRef(
        val url: String,
        val html_url: String,
        val diff_url: String,
        val patch_url: String
    )

    class GitHubUser(
        val id: Int,
        val login: String,
        val node_id: String,
        val avatar_url: String,
        val gravatar_id: String,
        val html_url: String,
        val type: String,
        val site_admin: Boolean
    )

    class GitHubLabel(
        val id: Int,
        val node_id: String,
        val name: String,
        val description: String,
        val color: String,
        val default: Boolean
    )

    class GitHubMilestone(
        val id: Int,
        val node_id: String,
        val html_url: String,
        val number: Int,
        val state: String,
        val title: String,
        val description: String,
        val creator: GitHubUser,
        val open_issues: Int,
        val closed_issues: Int,
        val created_at: String,
        val updated_at: String,
        val closed_at: String,
        val due_on: String
    )

}
