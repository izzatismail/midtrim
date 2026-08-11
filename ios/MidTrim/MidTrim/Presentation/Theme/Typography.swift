import SwiftUI

enum AppFont {
    static let regular = "Nunito-Regular"
    static let semiBold = "Nunito-SemiBold"
    static let bold = "Nunito-Bold"
}

extension Font {
    static let displayLarge = Font.custom(AppFont.bold, size: 32, relativeTo: .largeTitle)
    static let titleLarge = Font.custom(AppFont.semiBold, size: 22, relativeTo: .title)
    static let bodyLarge = Font.custom(AppFont.regular, size: 17, relativeTo: .body)
    static let body = Font.custom(AppFont.regular, size: 15, relativeTo: .body)
    static let caption = Font.custom(AppFont.regular, size: 13, relativeTo: .caption)
    static let buttonLabel = Font.custom(AppFont.semiBold, size: 16, relativeTo: .body)
}

enum AppSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 48
    static let cornerCard: CGFloat = 16
    static let cornerButton: CGFloat = 12
    static let cornerPill: CGFloat = 999
    static let minTapTarget: CGFloat = 44
}
