package com.example.project2

data class Item(
    val title: String,
    val comment: String,
    val photo: String?,
    val price: Double,
    val category: String,
    val link: String,
    val rating: Int // דירוג הכוכבים
)
object ItemManager{
    val items:MutableList<Item> = mutableListOf()
    fun  add(item: Item){
        items.add(item)
    }

    fun remove(index:Int){
        items.removeAt(index)
    }
}