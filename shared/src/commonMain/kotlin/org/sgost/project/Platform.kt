package org.sgost.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform