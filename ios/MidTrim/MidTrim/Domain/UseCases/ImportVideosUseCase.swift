import Foundation

struct ImportVideosUseCase {
    private let metadataService: VideoMetadataServiceProtocol
    private let entitlementCache: FetchEntitlementStatusUseCase

    init(metadataService: VideoMetadataServiceProtocol, entitlementCache: FetchEntitlementStatusUseCase) {
        self.metadataService = metadataService
        self.entitlementCache = entitlementCache
    }

    private var maxVideos: Int { 10 }
    private var maxVideosPaid: Int { 20 }

    func execute(videoURIs: [String]) async throws -> [VideoMetadata] {
        guard !videoURIs.isEmpty else {
            throw ImportVideoError.emptySelection
        }

        let isPaid = await entitlementCache.isPaidUser
        let cap = isPaid ? maxVideosPaid : maxVideos

        guard videoURIs.count <= cap else {
            throw ImportVideoError.exceedsTierCap(max: cap, attempted: videoURIs.count)
        }

        var metadataList: [VideoMetadata] = []
        for uri in videoURIs {
            let metadata = try await metadataService.fetchMetadata(for: uri)
            metadataList.append(metadata)
        }
        return metadataList
    }
}
