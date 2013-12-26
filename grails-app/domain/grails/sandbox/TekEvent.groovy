package grails.sandbox

class TekEvent {



    String city
    String name
    TekUser organizer
    String venue
    Date startDate
    Date endDate
    String description

    static constraints = {
        name()
        organizer()
        city()
        venue()
        description(maxSize: 5000)
        startDate()
        endDate()
    }

    String toString(){
        "$city, $name"
    }

}
