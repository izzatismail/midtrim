import CoreGraphics

extension ExportQuality {
    func toTierString() -> String {
        switch self {
        case .free720p: return "free_720p"
        case .paid(let preset, _):
            switch preset {
            case .small: return "paid_720p"
            case .balanced: return "paid_1080p"
            case .best: return "paid_original"
            }
        }
    }

    static func from(tierString: String, sourceResolution: CGSize) -> ExportQuality {
        switch tierString {
        case "paid_720p": return .paid(preset: .small, sourceResolution: sourceResolution)
        case "paid_1080p": return .paid(preset: .balanced, sourceResolution: sourceResolution)
        case "paid_original": return .paid(preset: .best, sourceResolution: sourceResolution)
        default: return .free720p
        }
    }
}
