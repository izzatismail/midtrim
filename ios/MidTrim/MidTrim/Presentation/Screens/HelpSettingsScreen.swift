import SwiftUI

struct HelpSettingsScreen: View {
    let onRestore: () -> Void
    let onPrivacyPolicy: () -> Void
    let onLicenses: () -> Void
    let onDismiss: () -> Void
    let appVersion: String
    let restoreResult: String?

    init(onRestore: @escaping () -> Void, onPrivacyPolicy: @escaping () -> Void = {}, onLicenses: @escaping () -> Void = {}, onDismiss: @escaping () -> Void, appVersion: String = "1.0.0", restoreResult: String? = nil) {
        self.onRestore = onRestore
        self.onPrivacyPolicy = onPrivacyPolicy
        self.onLicenses = onLicenses
        self.onDismiss = onDismiss
        self.appVersion = appVersion
        self.restoreResult = restoreResult
    }

    var body: some View {
        VStack(spacing: AppSpacing.lg) {
            Spacer()
            Button("Restore Purchases", action: onRestore).buttonStyle(.borderedProminent).tint(.accentPrimary)
            if let restoreResult { Text(restoreResult).font(.caption).foregroundColor(.textSecondary) }
            Spacer()
            Button("Privacy Policy", action: onPrivacyPolicy).font(.caption).foregroundColor(.textSecondary)
            Button("Open Source Licenses", action: onLicenses).font(.caption).foregroundColor(.textSecondary)
            Text("Version \(appVersion)").font(.caption).foregroundColor(.textSecondary)
        }
        .background(Color.bgPrimary)
        .navigationTitle("Help & Settings")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Close", action: onDismiss)
            }
        }
    }
}
