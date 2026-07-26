import Foundation
import SwiftData

@Model
final class SourceVideoItem {
    @Attribute(.unique) var id: UUID
    var sourceVideoURI: String
    var sourceVideoDuration: Double
    var sourceFileSize: Int64?
    var orderIndex: Int
    var trimStartTime: Double
    var trimEndTime: Double
    var project: Project?

    init(
        id: UUID = UUID(),
        sourceVideoURI: String,
        sourceVideoDuration: Double,
        sourceFileSize: Int64? = nil,
        orderIndex: Int,
        trimStartTime: Double,
        trimEndTime: Double
    ) {
        self.id = id
        self.sourceVideoURI = sourceVideoURI
        self.sourceVideoDuration = sourceVideoDuration
        self.sourceFileSize = sourceFileSize
        self.orderIndex = orderIndex
        self.trimStartTime = trimStartTime
        self.trimEndTime = trimEndTime
    }
}
