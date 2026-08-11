import SwiftUI

struct VideoSelectionScreen: View {
    @ObservedObject var viewModel: VideoSelectionViewModel
    let onContinue: () -> Void
    let onBack: () -> Void
    let onUpgrade: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.uiState.selectedVideos.isEmpty {
                emptyState
            } else {
                videoList
            }
        }
        .background(Color.bgPrimary)
        .navigationTitle("Select Videos")
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("Back", action: onBack)
            }
        }
        .onAppear { viewModel.initialize() }
    }

    private var emptyState: some View {
        VStack(spacing: AppSpacing.md) {
            Spacer()
            Text("No videos selected").font(.bodyLarge).foregroundColor(.textSecondary)
            Button("Add Videos") { viewModel.importVideos(["placeholder"]) }
                .buttonStyle(.borderedProminent).tint(.accentPrimary)
            Spacer()
        }
    }

    private var videoList: some View {
        VStack(spacing: AppSpacing.sm) {
            List {
                ForEach(viewModel.uiState.selectedVideos, id: \.uri) { video in
                    let index = viewModel.uiState.selectedVideos.firstIndex(where: { $0.uri == video.uri }) ?? 0
                    HStack {
                        VStack(alignment: .leading, spacing: AppSpacing.xs) {
                            Text(video.uri.components(separatedBy: "/").last ?? "")
                                .font(.body).lineLimit(1)
                            Text("\(Int(video.duration))s · \(video.format)")
                                .font(.caption).foregroundColor(.textSecondary)
                        }
                        Spacer()
                        Button { viewModel.removeVideo(at: index) } label: {
                            Image(systemName: "xmark.circle.fill").foregroundColor(.errorColor)
                        }
                    }
                    .padding(.vertical, AppSpacing.xs)
                    .listRowBackground(Color.bgElevated)
                }
                .onMove { source, destination in viewModel.reorder(from: source, to: destination) }
                addVideoButton.listRowBackground(Color.clear)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
            mergedDurationBanner
            Button("Continue", action: onContinue)
                .buttonStyle(.borderedProminent).tint(.accentPrimary)
                .disabled(viewModel.uiState.selectedVideos.isEmpty)
                .padding(.horizontal, AppSpacing.md)
                .padding(.bottom, AppSpacing.md)
        }
    }

    private var addVideoButton: some View {
        let isAtCap = !viewModel.uiState.isPaidUser && viewModel.uiState.selectedVideos.count >= 10
        return Button(action: isAtCap ? onUpgrade : { viewModel.importVideos(["placeholder"]) }) {
            HStack {
                Spacer()
                Text(isAtCap ? "+ Locked" : "+ Add Video").font(.bodyLarge)
                    .foregroundColor(isAtCap ? .premiumAccent : .textPrimary)
                if isAtCap { Text("🔒") }
                Spacer()
            }
            .padding(AppSpacing.md)
            .background(Color.bgSurface.opacity(0.5))
            .cornerRadius(AppSpacing.cornerButton)
        }
    }

    private var mergedDurationBanner: some View {
        Text("Merged length: \(Int(viewModel.uiState.mergedDuration))s")
            .font(.bodyLarge).foregroundColor(.textSecondary)
            .padding(AppSpacing.sm).frame(maxWidth: .infinity)
            .background(Color.bgSurface)
    }
}
