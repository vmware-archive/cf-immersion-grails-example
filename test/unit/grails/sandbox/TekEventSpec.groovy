package grails.sandbox

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(TekEvent)
class TekEventSpec extends Specification {
    def tekEvent = new TekEvent(city: 'Rockaway', name: 'springone')
    def answer = "Rockaway, springone"


    def setup() {
    }

    def cleanup() {
    }

    void "The toString should return the city and name of the event"() {
            when:
                def str = tekEvent.toString()
            then:
                str == answer
    }
}
