package com.phlourenco.utils

import org.openqa.selenium.chrome.ChromeDriver

interface IIntEnum {
    fun getValue(): Int
}

interface IStrEnum {
    fun getTitle(): String
}

fun ChromeDriver.closeAllTabs() {
    val tabs = ArrayList(windowHandles)
    tabs.reversed().forEachIndexed { index, _ ->
        switchTo().window(tabs[index])
        close()
    }
}