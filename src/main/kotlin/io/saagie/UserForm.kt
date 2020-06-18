package io.saagie

import arrow.core.*
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative

sealed class Error(val cause: String) {
    object NameTooShort : Error("Name is too short")
    object PasswordNotStrongEnough : Error("Password is not strong enough")
    object TooYoung : Error("User should be older than 18")
}

data class UserForm(val userName: String, val password: String, val age: Int) {

    private fun isComplexPassword(): Boolean =
        this.password
            .toCharArray()
            .filterNot { it.isLetterOrDigit() }
            .size >= 2

    fun validateUserName(): ValidatedNel<Error, String> =
        if (userName.length < 2) {
            Error.NameTooShort.invalidNel()
        } else userName.validNel()

    fun validatePassword(): ValidatedNel<Error, String> =
        if (isComplexPassword()) {
            password.validNel()
        } else {
            Error.PasswordNotStrongEnough.invalidNel()
        }

    fun validateAge(): ValidatedNel<Error, Int> =
        if (age < 18) {
            Error.TooYoung.invalidNel()
        } else {
            age.validNel()
        }
}

data class User(val userName: String, val password: String, val age: Int)

fun getValidatedUserFromUserForm(userForm: UserForm): Either<Nel<Error>, User> =
    ValidatedNel.applicative(Nel.semigroup<Error>()).mapN(
        userForm.validateUserName(),
        userForm.validatePassword(),
        userForm.validateAge()
    ) { (userName, password, age) -> User(userName, password, age) }
        .fix()
        .toEither()

fun displayUserOrErrors(userForm: UserForm) =
    getValidatedUserFromUserForm(userForm)
        .mapLeft { error -> println("There were errors validating the UserForm:${error.map { it.cause }}") }
        .map { println("Getting a fully validated User: $it") }

fun main() {
    val userForm = UserForm("a", "password", 30)

    displayUserOrErrors(userForm)

    val userForm2 = UserForm("Richard", "S@@agie!", 45)

    displayUserOrErrors(userForm2)
}