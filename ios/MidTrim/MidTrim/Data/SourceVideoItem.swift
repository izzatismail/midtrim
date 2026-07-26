import Foundation
import SwiftData

@Model
final class SourceVideoItem {
    var sourceUri: String
    var orderIndex: Int
    var durationMs: Int64
    var fileSize: Int64
    var resolution: String

    var project: Project?

    init(
        sourceUri: String,
        orderIndex: Int,
        durationMs: Int64,
        fileSize: Int64,
        resolution: String
    ) {
        self.sourceUri = sourceUri
        self.orderIndex = orderIndex
        self.durationMs = durationMs
        self.fileSize = fileSize
        self.resolution = resolution
    }
}
