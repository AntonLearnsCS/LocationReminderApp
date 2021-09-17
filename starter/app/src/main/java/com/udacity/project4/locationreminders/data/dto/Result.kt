package com.udacity.project4.locationreminders.data.dto


/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and statusCode
 */
//TODO: Where can I go to get more information on the use of this sealed class?
//So you have the option to use one of each data class when a data type is wrapped in Result<>
//"data" in: data class Success<out T : Any>(val data: T) : Result<T>() refers to any data type wrapped by Result. In the case of
//"Result<List<ReminderDTO>>", data refers to <List<ReminderDTO>>

//We are able to wrap Result around any data type b/c we specify the generic T as being of type "Any". So the "T" in "(val data: T)"
//can allows "data" to be any data type that extends (via keyword out) "Any". If we only wanted Result to encapsulate data types that
//are super to say "Integer", we could do: data class Success<in T : Number>(val data: T) : Result<T>()

// "<out T : Any>" is the signature, so that when we use Result, we can create only objects that extend the
// "Any" i.e val mResult : Result<Any> = Result<listOf(1,2,3)>

//source: https://stackoverflow.com/questions/44298702/what-is-out-keyword-in-kotlin
sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val message: String?, val statusCode: Int? = null) :
        Result<Nothing>()
}

/**
 * `true` if [Result] is of type [Success] & holds non-null [Success.data].
 */

val Result<*>.succeeded
    get() = this is Result.Success