package com.maestro.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

object JwtConfig {
    lateinit var secret: String
    lateinit var issuer: String
    lateinit var audience: String
    var expirationDays: Long = 30

    fun makeToken(userId: Long): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + expirationDays * 86_400_000L))
        .sign(Algorithm.HMAC256(secret))
}

private const val DEV_SECRET = "maestro-dev-secret-change-in-production"

fun Application.configureAuth() {
    val config = environment.config
    JwtConfig.secret = config.property("jwt.secret").getString()
    // Fail fast if the dev fallback secret leaks into a real deployment
    if (JwtConfig.secret == DEV_SECRET && System.getenv("RAILWAY_ENVIRONMENT") != null) {
        error("JWT_SECRET env var is not set — refusing to start in production with the dev secret")
    }
    JwtConfig.issuer = config.property("jwt.issuer").getString()
    JwtConfig.audience = config.property("jwt.audience").getString()
    JwtConfig.expirationDays = config.propertyOrNull("jwt.expirationDays")?.getString()?.toLong() ?: 30

    install(Authentication) {
        jwt("jwt-auth") {
            realm = "Maestro"
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.secret))
                    .withAudience(JwtConfig.audience)
                    .withIssuer(JwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asLong() != null)
                    JWTPrincipal(credential.payload) else null
            }
        }
    }
}

fun JWTPrincipal.userId(): Long = payload.getClaim("userId").asLong()
