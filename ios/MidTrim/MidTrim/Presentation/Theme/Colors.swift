import SwiftUI

extension Color {
    static let bgPrimary = Color(hex: "0A0A0B")
    static let bgElevated = Color(hex: "18181B")
    static let bgSurface = Color(hex: "242428")
    static let accentPrimary = Color(hex: "5B5FEF")
    static let accentPrimaryPressed = Color(hex: "4548C4")
    static let textPrimary = Color(hex: "FAFAFA")
    static let textSecondary = Color(hex: "A1A1AA")
    static let textDisabled = Color(hex: "52525B")
    static let success = Color(hex: "34D399")
    static let errorColor = Color(hex: "F87171")
    static let divider = Color(hex: "27272A")
    static let premiumAccent = Color(hex: "F5B841")

    static let bgPrimaryLight = Color(hex: "FFFFFF")
    static let bgElevatedLight = Color(hex: "F4F4F5")
    static let bgSurfaceLight = Color(hex: "E4E4E7")
    static let textPrimaryLight = Color(hex: "18181B")
    static let textSecondaryLight = Color(hex: "71717A")
    static let textDisabledLight = Color(hex: "A1A1AA")
    static let successLight = Color(hex: "059669")
    static let errorColorLight = Color(hex: "DC2626")
    static let dividerLight = Color(hex: "E4E4E7")
    static let premiumAccentLight = Color(hex: "B8860B")

    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b: UInt64
        (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: 1
        )
    }
}
