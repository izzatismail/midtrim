import Foundation

struct MergeVideoSegmentsUseCase {
    private let trimmer: VideoTrimmerProtocol
    private let qualityResolver: ResolveExportQualityUseCase

    init(trimmer: VideoTrimmerProtocol, qualityResolver: ResolveExportQualityUseCase) {
        self.trimmer = trimmer
        self.qualityResolver = qualityResolver
    }

    func execute(segmentURIs: [String], isPaidUser: Bool, sourceResolution: CGSize) async throws -> String {
        guard !segmentURIs.isEmpty else {
            throw TrimError.mergeFailed("No segments to merge.")
        }

        let quality = qualityResolver.execute(isPaidUser: isPaidUser, sourceResolution: sourceResolution)
        return try await trimmer.merge(segmentURIs: segmentURIs, outputQuality: quality)
    }
}
