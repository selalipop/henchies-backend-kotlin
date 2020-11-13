package util

import com.bugsnag.Bugsnag
import mu.KotlinLogging

val logger = KotlinLogging.logger {}
var bugsnag = Bugsnag("your-api-key-here", false)
