import Foundation

struct TrimVideoUseCase {
    private let trimmer: VideoTrimmerProtocol
    private let trimValidator: ValidateTrimDurationUseCase
    private let windowCalculator: CalculateTrimWindowUseCase

    init(trimmer: VideoTrimmerProtocol, trimValidator: ValidateTrimDurationUseCase, windowCalculator: CalculateTrimWindowUseCase) {
        self.trimmer = trimmer
        self.trimValidator = trimValidator
        self.windowCalculator = windowCalculator
    }

    func execute(sourceURI: String, duration: Double, trimDuration: Double, isPaidUser: Bool) async throws -> String {
        guard !sourceURI.isEmpty else {
            throw TrimError.fileNotFound("")
        }
        try trimValidator.execute(trimDuration: trimDuration, isPaidUser: isPaidUser)

        let window = try windowCalculator.execute(videoDuration: duration, trimDuration: trimDuration)
        return try await trimmer.trim(sourceURI: sourceURI, startTime: window.startTime, endTime: window.endTime)
    }
}
