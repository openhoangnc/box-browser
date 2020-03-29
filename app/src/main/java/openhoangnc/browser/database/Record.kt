package openhoangnc.browser.database

class Record {
    var title: String?

    var url: String?

    var time: Long

    constructor() {
        title = null
        url = null
        time = 0L
    }

    constructor(title: String?, url: String?, time: Long) {
        this.title = title
        this.url = url
        this.time = time
    }
}