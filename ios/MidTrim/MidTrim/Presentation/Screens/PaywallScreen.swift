import SwiftUI

struct PaywallScreen: View {
    let onPurchase: () -> Void
    let onRestore: () -> Void
    let onDismiss: () -> Void
    let isLoading: Bool
    let error: String?

    init(onPurchase: @escaping () -> Void, onRestore: @escaping () -> Void, onDismiss: @escaping () -> Void, isLoading: Bool = false, error: String? = nil) {
        self.onPurchase = onPurchase
        self.onRestore = onRestore
        self.onDismiss = onDismiss
        self.isLoading = isLoading
        self.error = error
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: AppSpacing.lg) {
                Spacer()
                Text("Unlock MidTrim").font(.titleLarge).foregroundColor(.premiumAccent)
                VStack(alignment: .leading, spacing: AppSpacing.md) {
                    BenefitRow(text: "Custom trim duration (1–5s)")
                    BenefitRow(text: "Full quality exports (up to 4K)")
                    BenefitRow(text: "Up to 20 videos per project")
                }
                .padding(.horizontal, AppSpacing.md)
                Text("$5.00 — one-time purchase").font(.body).foregroundColor(.textSecondary)
                Button(action: onPurchase) {
                    if isLoading {
                        ProgressView()
                    } else {
                        Text("Unlock MidTrim").font(.buttonLabel)
                    }
                }
                .buttonStyle(.borderedProminent).tint(.premiumAccent).disabled(isLoading)
                .padding(.horizontal, AppSpacing.md)
                Button("Restore Purchases", action: onRestore).font(.body).foregroundColor(.accentPrimary)
                if let error { Text(error).font(.caption).foregroundColor(.errorColor) }
                Spacer()
            }
            .background(Color.bgPrimary)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Close", action: onDismiss)
                }
            }
        }
    }
}

private struct BenefitRow: View {
    let text: String
    var body: some View {
        HStack(spacing: AppSpacing.sm) {
            Text("✓").font(.bodyLarge).foregroundColor(.premiumAccent)
            Text(text).font(.bodyLarge)
        }
        .padding(AppSpacing.md).frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.bgElevated).cornerRadius(AppSpacing.cornerButton)
    }
}
