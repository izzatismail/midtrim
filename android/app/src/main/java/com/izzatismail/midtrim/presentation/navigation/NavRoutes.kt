package com.izzatismail.midtrim.presentation.navigation

sealed class Route(val route: String) {
    data object ProjectList : Route("project_list")
    data object VideoSelection : Route("video_selection")
    data object TrimDuration : Route("trim_duration")
    data object NameProject : Route("name_project")
    data object Paywall : Route("paywall")
    data object HelpSettings : Route("help_settings")
    data object Licenses : Route("licenses")
}
