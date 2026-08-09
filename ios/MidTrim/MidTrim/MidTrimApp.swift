//
//  MidTrimApp.swift
//  MidTrim
//
//  Created by Mohd Izzat Ismail Hashim on 22/07/2026.
//

import SwiftUI
import SwiftData

@main
struct MidTrimApp: App {
    let modelContainer: ModelContainer = {
        do {
            return try ModelContainer(for: Project.self, SourceVideoItem.self)
        } catch {
            fatalError("Failed to create ModelContainer: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .modelContainer(modelContainer)
        }
    }
}
