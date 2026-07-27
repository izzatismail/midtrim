import Foundation

struct ReorderVideosUseCase {
    func execute<T>(_ items: [T], to newOrder: [Int]) throws -> [T] {
        guard !items.isEmpty else { throw ReorderVideosError.emptyList }
        guard newOrder.count == items.count,
              Set(newOrder) == Set(0..<items.count) else {
            throw ReorderVideosError.invalidIndices
        }
        return newOrder.map { items[$0] }
    }
}
