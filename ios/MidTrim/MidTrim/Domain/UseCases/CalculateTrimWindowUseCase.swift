import Foundation

struct CalculateTrimWindowUseCase {
    func execute(videoDuration: Double, trimDuration: Double) throws -> TrimWindow {
        guard videoDuration >= trimDuration else {
            throw TrimWindowError.videoShorterThanTrimDuration(
                videoDuration: videoDuration,
                trimDuration: trimDuration
            )
        }

        let center = videoDuration / 2
        let halfTrim = trimDuration / 2
        let startTime = max(0, center - halfTrim)
        let endTime = min(videoDuration, center + halfTrim)

        return TrimWindow(startTime: startTime, endTime: endTime)
    }
}
