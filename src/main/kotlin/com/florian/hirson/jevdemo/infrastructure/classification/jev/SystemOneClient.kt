package com.florian.hirson.jevdemo.infrastructure.classification.jev

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

/** Declarative HTTP client for jev's System One endpoint — see https://docs.typesafe.ai/api.md. */
interface SystemOneClient {
    @PostExchange("/v1/systemone")
    fun classify(@RequestBody request: SystemOneRequest): SystemOneResponse
}
