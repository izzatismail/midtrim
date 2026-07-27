import XCTest
@testable import MidTrim

final class ValidateTrimDurationUseCaseTests: XCTestCase {
    let useCase = ValidateTrimDurationUseCase()

    // MARK: - Free tier

    func testFreeTierAccepts1s() {
        XCTAssertNoThrow(try useCase.execute(trimDuration: 1, isPaidUser: false))
    }

    func testFreeTierAccepts2s() {
        XCTAssertNoThrow(try useCase.execute(trimDuration: 2, isPaidUser: false))
    }

    func testFreeTierAccepts3s() {
        XCTAssertNoThrow(try useCase.execute(trimDuration: 3, isPaidUser: false))
    }

    func testFreeTierRejectsCustom() {
        XCTAssertThrowsError(try useCase.execute(trimDuration: 4, isPaidUser: false)) { error in
            guard case let .freeTierRejectsCustomDuration(duration) = error as? ValidateTrimDurationError else {
                return XCTFail("Expected freeTierRejectsCustomDuration")
            }
            XCTAssertEqual(duration, 4)
        }
    }

    // MARK: - Paid tier

    func testPaidTierAccepts1s() {
        XCTAssertNoThrow(try useCase.execute(trimDuration: 1, isPaidUser: true))
    }

    func testPaidTierAccepts5s() {
        XCTAssertNoThrow(try useCase.execute(trimDuration: 5, isPaidUser: true))
    }

    func testPaidTierAcceptsCustomValue() {
        XCTAssertNoThrow(try useCase.execute(trimDuration: 3.7, isPaidUser: true))
    }

    // MARK: - Both tiers

    func testBelowMinimumThrows() {
        XCTAssertThrowsError(try useCase.execute(trimDuration: 0.5, isPaidUser: true)) { error in
            guard case let .belowMinimumDuration(duration) = error as? ValidateTrimDurationError else {
                return XCTFail("Expected belowMinimumDuration")
            }
            XCTAssertEqual(duration, 0.5)
        }
    }

    func testExceedsMaxThrows() {
        XCTAssertThrowsError(try useCase.execute(trimDuration: 5.5, isPaidUser: true)) { error in
            guard case let .exceedsMaxDuration(duration) = error as? ValidateTrimDurationError else {
                return XCTFail("Expected exceedsMaxDuration")
            }
            XCTAssertEqual(duration, 5.5)
        }
    }

    // MARK: - isAllowed convenience

    func testIsAllowedFree() {
        XCTAssertTrue(useCase.isAllowed(trimDuration: 1, isPaidUser: false))
        XCTAssertTrue(useCase.isAllowed(trimDuration: 2, isPaidUser: false))
        XCTAssertTrue(useCase.isAllowed(trimDuration: 3, isPaidUser: false))
        XCTAssertFalse(useCase.isAllowed(trimDuration: 4, isPaidUser: false))
        XCTAssertFalse(useCase.isAllowed(trimDuration: 0.5, isPaidUser: false))
    }

    func testIsAllowedPaid() {
        XCTAssertTrue(useCase.isAllowed(trimDuration: 1, isPaidUser: true))
        XCTAssertTrue(useCase.isAllowed(trimDuration: 3.5, isPaidUser: true))
        XCTAssertTrue(useCase.isAllowed(trimDuration: 5, isPaidUser: true))
        XCTAssertFalse(useCase.isAllowed(trimDuration: 0.5, isPaidUser: true))
        XCTAssertFalse(useCase.isAllowed(trimDuration: 5.5, isPaidUser: true))
    }
}
