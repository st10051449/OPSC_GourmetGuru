package com.opsc7311poe.gourmetguru_opscpoe

data class Meal(
    var name: String = "",
    var ingredients: List<String> = emptyList(),
    var image: String = "",
    var duration: String = "",
    var steps: List<String> = emptyList()
) {
    constructor() : this("", emptyList(), "", "", emptyList()) // Default constructor
}
