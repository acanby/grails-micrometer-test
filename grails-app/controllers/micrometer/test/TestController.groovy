package micrometer.test

import io.micrometer.core.annotation.Timed

@Timed
class TestController {
    static responseFormats = ['json']

    def index() {
        int seconds = new Random().nextInt(5)
        sleep(seconds * 1000)
        respond(seconds: seconds)
    }
}