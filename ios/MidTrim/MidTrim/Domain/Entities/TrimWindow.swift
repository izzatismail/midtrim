import Foundation

struct TrimWindow: Equatable {
    let startTime: Double
    let endTime: Double

    var duration: Double {
        endTime - startTime
    }
}
