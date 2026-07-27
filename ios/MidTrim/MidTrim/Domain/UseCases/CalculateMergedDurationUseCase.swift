import Foundation

struct CalculateMergedDurationUseCase {
    func execute(trimDuration: Double, videoCount: Int) -> Double {
        guard trimDuration >= 0, videoCount >= 0 else {
            return 0
        }
        return trimDuration * Double(videoCount)
    }
}
