package com.izzatismail.midtrim.presentation.navigation

sealed class Route(val route: String) {
    data object ProjectList : Route("project_list")
    data object VideoSelection : Route("video_selection")
    data object TrimDuration : Route("trim_duration")
    data object NameProject : Route("name_project")
    data object Paywall : Route("paywall")
    data object HelpSettings : Route("help_settings")
}

object Spacing {
    val xs = 4
    val sm = 8
    val md = 16
    val lg = 24
    val xl = 32
    val xxl = 48
    val cornerCard = 16
    val cornerButton = 12
    val cornerPill = 999
    val minTapTarget = 48
}