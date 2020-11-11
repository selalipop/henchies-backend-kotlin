import com.bugsnag.Bugsnag
import mu.KotlinLogging
import java.lang.Error

val logger = KotlinLogging.logger {}
var bugsnag = Bugsnag("your-api-key-here", false)

fun Error.logged() : Error {
    logger.error(this) { this.message }
    return this
}