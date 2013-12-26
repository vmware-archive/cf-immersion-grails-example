package grails.sandbox

class Drink {

    String name
    int size
    boolean hot

    static constraints = {
        name()
        size(max: 3, min: 1)
    }
}
