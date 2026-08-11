import SwiftUI

struct TrimDurationScreen: View {
    let selectedDuration: Double
    let isPaidUser: Bool
    let onDurationSelected: (Double) -> Void
    let onCustomTap: () -> Void
    let onContinue: () -> Void
    let onBack: () -> Void

    private let durations: [Double] = [1.0, 2.0, 3.0]

    var body: some View {
        VStack(spacing: AppSpacing.lg) {
            Text("Select trim duration").font(.titleLarge).padding(.top, AppSpacing.xl)
            HStack(spacing: AppSpacing.sm) {
                ForEach(durations, id: \.self) { duration in
                    Button { onDurationSelected(duration) } label: {
                        Text("\(Int(duration))s").font(.buttonLabel).frame(maxWidth: .infinity)
                            .padding(.vertical, AppSpacing.sm)
                            .background(selectedDuration == duration ? Color.accentPrimary : Color.bgSurface)
                            .foregroundColor(selectedDuration == duration ? .textPrimary : .textSecondary)
                            .cornerRadius(AppSpacing.cornerButton)
                    }
                }
            }
            .padding(.horizontal, AppSpacing.md)
            Button(action: onCustomTap) {
                HStack {
                    Text("Custom").font(.bodyLarge)
                    if !isPaidUser { Text("🔒") }
                }
                .frame(maxWidth: .infinity).padding(AppSpacing.md)
                .background(isPaidUser ? Color.bgElevated : Color.bgSurface.opacity(0.5))
                .foregroundColor(isPaidUser ? .textPrimary : .premiumAccent)
                .cornerRadius(AppSpacing.cornerButton)
            }
            .padding(.horizontal, AppSpacing.md)
            qualityBadge
            Spacer()
            Button("Preview Trim", action: onContinue)
                .buttonStyle(.borderedProminent).tint(.accentPrimary)
                .padding(.horizontal, AppSpacing.md).padding(.bottom, AppSpacing.md)
        }
        .background(Color.bgPrimary)
        .navigationTitle("Trim Duration")
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("Back", action: onBack)
            }
        }
    }

    private var qualityBadge: some View {
        Button(action: isPaidUser ? {} : onCustomTap) {
            HStack(spacing: AppSpacing.xs) {
                Text(isPaidUser ? "Original Quality" : "720p").font(.body)
                if !isPaidUser { Text("🔒") }
            }
            .padding(.horizontal, AppSpacing.md).padding(.vertical, AppSpacing.sm)
            .background(Color.bgSurface)
            .foregroundColor(isPaidUser ? .textSecondary : .premiumAccent)
            .cornerRadius(AppSpacing.cornerButton)
        }
        .disabled(isPaidUser)
    }
}
