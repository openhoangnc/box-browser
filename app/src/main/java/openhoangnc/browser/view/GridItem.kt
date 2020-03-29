package openhoangnc.browser.view

class GridItem {
    var title: String?

    var url: String?

    var filename: String?

    var ordinal: Int

    constructor() {
        title = null
        url = null
        filename = null
        ordinal = -1
    }

    constructor(title: String?, url: String?, filename: String?, ordinal: Int) {
        this.title = title
        this.url = url
        this.filename = filename
        this.ordinal = ordinal
    }
}