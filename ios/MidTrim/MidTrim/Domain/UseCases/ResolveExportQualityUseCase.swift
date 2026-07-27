import Foundation
import CoreGraphics

enum ExportQuality: Equatable {
    case free720p
    case paidOriginal(resolution: CGSize)
}

struct ResolveExportQualityUseCase {
    func execute(isPaidUser: Bool, sourceResolution: CGSize) -> ExportQuality {
        if isPaidUser {
            return .paidOriginal(resolution: sourceResolution)
        }
        return .free720p
    }
}
