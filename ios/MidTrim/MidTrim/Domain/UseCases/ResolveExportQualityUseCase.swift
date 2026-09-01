import Foundation
import CoreGraphics

enum ExportQualityPreset: String, CaseIterable {
    case small = "Small"
    case balanced = "Balanced"
    case best = "Best"

    var maxHeight: Int {
        switch self {
        case .small: return 720
        case .balanced: return 1080
        case .best: return Int.max
        }
    }

    func effectiveMaxHeight(sourceHeight: Int) -> Int {
        self == .best ? sourceHeight : min(maxHeight, sourceHeight)
    }

    func subtitle(sourceWidth: Int, sourceHeight: Int) -> String {
        let res: Int
        let resText: String
        if self == .best {
            res = sourceWidth
            resText = "\(sourceWidth)p"
        } else {
            res = effectiveMaxHeight(sourceHeight: sourceHeight)
            resText = "\(res)p"
        }
        return switch self {
        case .small: "Quick share \u{00B7} \(resText)"
        case .balanced: "Good quality \u{00B7} \(resText)"
        case .best: "Source quality \u{00B7} \(resText)"
        }
    }

    static func availableFor(sourceHeight: Int) -> [ExportQualityPreset] {
        var presets: [ExportQualityPreset] = [.small, .best]
        if sourceHeight > 1080 { presets.insert(.balanced, at: 1) }
        return presets
    }
}

enum ExportQuality: Equatable {
    case free720p
    case paid(preset: ExportQualityPreset, sourceResolution: CGSize)
}

struct ResolveExportQualityUseCase {
    func execute(isPaidUser: Bool, preset: ExportQualityPreset?, sourceResolution: CGSize) -> ExportQuality {
        if isPaidUser {
            return .paid(preset: preset ?? .best, sourceResolution: sourceResolution)
        }
        return .free720p
    }
}
