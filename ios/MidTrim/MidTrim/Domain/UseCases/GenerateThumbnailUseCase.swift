import Foundation

struct GenerateThumbnailUseCase {
    private let frameExtractor: FrameExtractorProtocol

    init(frameExtractor: FrameExtractorProtocol) {
        self.frameExtractor = frameExtractor
    }

    func execute(from outputVideoURI: String) async throws -> String {
        try await frameExtractor.extractFrame(from: outputVideoURI, at: 0)
    }
}
