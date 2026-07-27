import Foundation

struct ValidateTrimDurationUseCase {
    private let freeTierAllowed: Set<Double> = [1, 2, 3]
    private let paidTierRange: ClosedRange<Double> = 1.0...5.0

    func execute(trimDuration: Double, isPaidUser: Bool) throws {
        guard paidTierRange.contains(trimDuration) else {
            if trimDuration < 1.0 {
                throw ValidateTrimDurationError.belowMinimumDuration(trimDuration)
            }
            throw ValidateTrimDurationError.exceedsMaxDuration(trimDuration)
        }

        if !isPaidUser {
            guard freeTierAllowed.contains(trimDuration) else {
                throw ValidateTrimDurationError.freeTierRejectsCustomDuration(trimDuration)
            }
        }
    }

    func isAllowed(trimDuration: Double, isPaidUser: Bool) -> Bool {
        (try? execute(trimDuration: trimDuration, isPaidUser: isPaidUser)) != nil
    }
}
