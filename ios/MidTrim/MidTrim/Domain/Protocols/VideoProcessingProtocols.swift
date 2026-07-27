import Foundation

protocol VideoTrimmerProtocol {
    func trim(sourceURI: String, startTime: Double, endTime: Double) async throws -> String
    func merge(segmentURIs: [String], outputQuality: ExportQuality) async throws -> String
}

protocol FrameExtractorProtocol {
    func extractFrame(from videoURI: String, at time: Double) async throws -> String
}

protocol VideoMetadataServiceProtocol {
    func fetchMetadata(for videoURI: String) async throws -> VideoMetadata
}
