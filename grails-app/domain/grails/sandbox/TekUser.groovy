package grails.sandbox

class TekUser {

    String fullName
    String userName
    String password
    String email
    String website
    String bio


    static constraints = {
        fullName()
        userName()
        email()
        website()
        bio(maxSize: 5000)
    }
}
