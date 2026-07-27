import XCTest
@testable import MidTrim

final class ReorderVideosUseCaseTests: XCTestCase {
    let useCase = ReorderVideosUseCase()

    func testReorderByIndexMap() throws {
        let items = ["A", "B", "C", "D"]
        let result = try useCase.execute(items, to: [2, 0, 3, 1])
        XCTAssertEqual(result, ["C", "A", "D", "B"])
    }

    func testReorderToSameOrder() throws {
        let items = ["A", "B", "C"]
        let result = try useCase.execute(items, to: [0, 1, 2])
        XCTAssertEqual(result, ["A", "B", "C"])
    }

    func testReorderWithSingleItem() throws {
        let items = ["A"]
        let result = try useCase.execute(items, to: [0])
        XCTAssertEqual(result, ["A"])
    }

    func testReorderEmptyListThrows() {
        let items: [String] = []
        XCTAssertThrowsError(try useCase.execute(items, to: [])) { error in
            XCTAssertEqual(error as? ReorderVideosError, .emptyList)
        }
    }

    func testReorderInvalidIndicesThrows() {
        let items = ["A", "B", "C"]
        XCTAssertThrowsError(try useCase.execute(items, to: [0, 1, 5])) { error in
            XCTAssertEqual(error as? ReorderVideosError, .invalidIndices)
        }
    }

    func testReorderMismatchedCountThrows() {
        let items = ["A", "B", "C"]
        XCTAssertThrowsError(try useCase.execute(items, to: [0, 1])) { error in
            XCTAssertEqual(error as? ReorderVideosError, .invalidIndices)
        }
    }
}
