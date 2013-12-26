import grails.sandbox.Drink
import grails.sandbox.TekEvent
import grails.sandbox.TekUser

class BootStrap {

    def init = { servletContext ->

        def user1 = new TekUser(fullName: 'James Gosling',
                email: 'jgosling@gmail.com',
                bio: '''The Father of Java....enough said!''',
                password: 'secret',
                userName: 'yo',
                website: 'gosling.wordpress.com')

        if (!user1.save()) {
            user1.errors.allErrors.each {error -> "ERROR IS:  ${error}"}
        }

        def user2 = new TekUser(fullName: 'Susan Something',
                email: 'susan@gmail.com',
                bio: '''Susan is awesome but has no experience.  Come listen
                        to her talk.
                        You won't regret it''',
                password: 'secret',
                userName: 'ye',
                website: 'susan.wordpress.com')

        user2.save()


        def event1 = new TekEvent(name: 'test1', venue: 'venue1', organizer: TekUser.findByFullName('James Gosling'),
                description: '''this
                                    is a description''',
                startDate: new Date(), endDate: new Date('5/5/2014'), city: 'Rockaway')
        if (!event1.save()) {
            event1.errors.allErrors.each {error -> println error}
        }




    }
    def destroy = {
    }
}
