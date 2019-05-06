package micrometer.test

class UrlMappings {

    static mappings = {
        "/test"(controller: 'test', action:'index')
    }
}
