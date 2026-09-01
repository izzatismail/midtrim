import SwiftUI

struct TrimDurationScreen: View {
    let selectedDuration: Double
    let isPaidUser: Bool
    let availablePresets: [ExportQualityPreset]
    let selectedPreset: ExportQualityPreset
    let sourceWidth: Int
    let sourceHeight: Int
    let onDurationSelected: (Double) -> Void
    let onCustomTap: () -> Void
    let onQualityPresetSelected: (ExportQualityPreset) -> Void
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
            qualitySelector
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

    @ViewBuilder
    private var qualitySelector: some View {
        if isPaidUser {
            VStack(alignment: .leading, spacing: AppSpacing.xs) {
                Text("Export Quality").font(.bodyLarge).padding(.horizontal, AppSpacing.md)
                ForEach(availablePresets, id: \.self) { preset in
                    Button { onQualityPresetSelected(preset) } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(preset.rawValue).font(.body)
                                Text(preset.subtitle(sourceWidth: sourceWidth, sourceHeight: sourceHeight))
                                    .font(.caption).foregroundColor(.textSecondary)
                            }
                            Spacer()
                            if preset == selectedPreset {
                                Image(systemName: "checkmark").foregroundColor(.accentPrimary)
                            }
                        }
                        .padding(AppSpacing.md)
                        .background(preset == selectedPreset ? Color.accentPrimary.opacity(0.15) : Color.bgSurface)
                        .cornerRadius(AppSpacing.cornerButton)
                    }
                    .padding(.horizontal, AppSpacing.md)
                }
            }
        } else {
            Button(action: onCustomTap) {
                HStack(spacing: AppSpacing.xs) {
                    Text("720p").font(.body)
                    Text("🔒")
                }
                .padding(.horizontal, AppSpacing.md).padding(.vertical, AppSpacing.sm)
                .background(Color.bgSurface)
                .foregroundColor(.premiumAccent)
                .cornerRadius(AppSpacing.cornerButton)
            }
        }
    }
}
