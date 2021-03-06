package com.app.service.api

import android.content.Context
import com.app.R
import com.app.extensions.humanReadableTimeAgoSince
import com.app.service.ResetAppRunner
import com.app.service.error.network.HttpRequestError
import com.app.service.error.network.ServerErrorException
import com.app.service.json.JsonAdapter
import com.app.service.logger.Logger
import com.app.service.model.RepoModel
import com.app.service.vo.response.error.FieldsErrorException
import com.app.service.vo.response.error.FieldsErrorResponse
import com.app.service.vo.response.error.ForbiddenResponseError
import com.app.service.vo.response.error.RateLimitingResponseError
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import javax.inject.Inject

/**
 * Class that encapsulates http requests while also handling problems that can occur for each.
 *
 * The point of this class is to contain a function for all endpoints of an API. Each function does all of the deserialization and extra error handling. Let's say 1 endpoint can return a 400 error while others do not. The function for that certain endpoint will handle the 400 response for you.
 *
 * Each function returns a Kotlin Result type. This means that Rx onError will not be called. Either the Http function was successful (200-300 response with a deserialized VO object returned) or there was an error. The Result.failure exception will have a human readable error message that you can display to the user.
 *
 * If there is an error, this class will handle it. If it's a network error, it will simply give you that message so you can display that to the user. If it's a user error, the error will be optionally parsed from the response to be shown for the user and the UI can handle the error if it wants to. If it's a developer error, the error will be logged to notify the developer team to fix it.
 */
class GitHubApi @Inject constructor(
    @ApplicationContext private val context: Context,
    logger: Logger,
    private val service: GitHubService,
    private val resetAppRunner: ResetAppRunner
) : Api(context, logger) {

    fun getRepos(username: String): Single<Result<List<RepoModel>>> {
        return request(
            service.listRepos(username),
            /*
            extraErrorHandling is an optional parameter. It allows you to perform error handling that is specific to this 1 endoint in our API. 
            For an example, here we are handling the scenario of when we get a 404 status code. When you recieve a 404 response code from the GitHub API endpoint get repositories,
            a 404 means that the GitHub user does not exist. But if you get a 404 when trying to call another endpoint, a 404 might mean something entirely different or it means that 
            you the developer made a typo and the HTTP endpoint does not exist. 

            When there is a HTTP status code that means the same thing for all endpoints of an API, we handle that status code in the function `handleUnsuccessfulStatusCode()` in this file. 
            When a status code means something different for each endpoint, we must handle that status code in the endpoint function like we are doing here. 
             */
            extraErrorHandling = { processedResponse ->
                when (processedResponse.statusCode) {
                    404 -> HttpRequestError.userError(ForbiddenResponseError.from(processedResponse.body!!).error_message, null)
                    // Handle all of the scenarios your endpoint could encounter. When you have exhausted all of the scenarios, return null for all other scenarios. 
                    else -> null
                }
            }
        )
    }

    override fun handleUnsuccessfulStatusCode(processedResponse: ProcessedResponse): HttpRequestError? {
        /**
         * Note: Conflict errors are status code 409. We do not want to handle *all* conflicts here. Let that be handled by the individual endpoint as each endpoint has a finite set of conflicts that could happen. The endpoints can then have a custom error type that is returned to the user that is parsed and the UI acts on it. Better then a global conflict handler where the UI must handle all situations.
         */
        return when (processedResponse.statusCode) {
            401 -> {
                resetAppRunner.deleteAllAndReset()
                HttpRequestError.userError(context.getString(R.string.unauthorized_relogin), null)
            }
            in 500..600 -> {
                HttpRequestError.developerError(context.getString(R.string.error_500_600_response_code), ServerErrorException("Host: ${processedResponse.url} is down. Code ${processedResponse.statusCode}"))
            }
            429 -> {
                val body: RateLimitingResponseError = JsonAdapter.fromJson(processedResponse.body!!)
                HttpRequestError.userError(context.getString(R.string.error_rate_limiting_response).format(body.error.next_valid_request_date.humanReadableTimeAgoSince), null)
            }
            422 -> { // developer error because the app should have client side code which restricts the input for the user so a 422 should not happen.
                val body: FieldsErrorResponse = JsonAdapter.fromJson(processedResponse.body!!)
                HttpRequestError.developerError(body.message, FieldsErrorException(body.message))
            }
            else -> null
        }
    }
}
