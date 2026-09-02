import SwiftUI

struct TrimDurationScreen: View {
    let selectedDuration: Double
    let isPaidUser: Bool
    let isCustomSelected: Bool
    let customDuration: Double
    let availablePresets: [ExportQualityPreset]
    let selectedPreset: ExportQualityPreset
    let sourceWidth: Int
    let sourceHeight: Int
    let onDurationSelected: (Double) -> Void
    let onCustomTap: () -> Void
    let onCustomDurationChanged: (Double) -> Void
    let onQualityPresetSelected: (ExportQualityPreset) -> Void
    let onContinue: () -> Void
    let onBack: () -> Void

    private let durations: [Double] = [1.0, 2.0, 3.0]
    private let step = VideoSelectionViewModel.customDurationStep
    private let minVal = VideoSelectionViewModel.customDurationMin
    private let maxVal = VideoSelectionViewModel.customDurationMax

    var body: some View {
        VStack(spacing: AppSpacing.lg) {
            Text("Select trim duration").font(.titleLarge).padding(.top, AppSpacing.xl)
            HStack(spacing: AppSpacing.sm) {
                ForEach(durations, id: \.self) { duration in
                    Button { onDurationSelected(duration) } label: {
                        Text("\(Int(duration))s").font(.buttonLabel).frame(maxWidth: .infinity)
                            .padding(.vertical, AppSpacing.sm)
                            .background(selectedDuration == duration && !isCustomSelected
                                ? Color.accentPrimary : Color.bgSurface)
                            .foregroundColor(selectedDuration == duration && !isCustomSelected
                                ? .textPrimary : .textSecondary)
                            .cornerRadius(AppSpacing.cornerButton)
                    }
                }
            }
            .padding(.horizontal, AppSpacing.md)
            Button(action: onCustomTap) {
                VStack(spacing: AppSpacing.sm) {
                    HStack {
                        if isCustomSelected && isPaidUser {
                            Text("Custom  ·  \(String(format: "%.1f", customDuration))s").font(.bodyLarge)
                        } else {
                            Text("Custom").font(.bodyLarge)
                        }
                        if !isPaidUser { Text("🔒") }
                    }
                    .frame(maxWidth: .infinity).padding(AppSpacing.md)
                    .background(isPaidUser ? Color.bgElevated : Color.bgSurface.opacity(0.5))
                    .foregroundColor(isPaidUser ? .textPrimary : .premiumAccent)
                    .cornerRadius(AppSpacing.cornerButton)

                    if isCustomSelected && isPaidUser {
                        CustomDurationStepper(
                            value: customDuration,
                            step: step,
                            range: minVal...maxVal,
                            onChange: onCustomDurationChanged
                        )
                        .padding(.horizontal, AppSpacing.md)
                        .padding(.bottom, AppSpacing.sm)
                    }
                }
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

struct CustomDurationStepper: View {
    let value: Double
    let step: Double
    let range: ClosedRange<Double>
    let onChange: (Double) -> Void

    var body: some View {
        HStack(spacing: AppSpacing.sm) {
            Button { onChange(value - step) } label: {
                Image(systemName: "minus")
                    .font(.title3)
                    .frame(width: 44, height: 44)
            }
            .disabled(value - step < range.lowerBound - 0.001)
            .buttonStyle(.bordered)

            Text(String(format: "%.1fs", value))
                .font(.title2.bold())
                .frame(minWidth: 60)
                .multilineTextAlignment(.center)

            Button { onChange(value + step) } label: {
                Image(systemName: "plus")
                    .font(.title3)
                    .frame(width: 44, height: 44)
            }
            .disabled(value + step > range.upperBound + 0.001)
            .buttonStyle(.bordered)
        }
        .padding(.vertical, AppSpacing.xs)
    }
}
