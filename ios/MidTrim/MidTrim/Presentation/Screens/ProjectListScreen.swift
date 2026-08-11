import SwiftUI

struct ProjectListScreen: View {
    @ObservedObject var viewModel: ProjectListViewModel
    let onNewProject: () -> Void
    let onUpgrade: () -> Void
    let onSettings: () -> Void

    var body: some View {
        ZStack {
            Color.bgPrimary.ignoresSafeArea()
            VStack(spacing: 0) {
                if !viewModel.uiState.isPaidUser {
                    upgradeBanner
                }
                if viewModel.uiState.isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if viewModel.uiState.projects.isEmpty {
                    emptyState
                } else {
                    projectList
                }
            }
        }
        .navigationTitle("Projects")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("...", action: onSettings).font(.titleLarge)
            }
            ToolbarItem(placement: .bottomBar) {
                Button(action: onNewProject) {
                    Image(systemName: "plus")
                        .font(.title)
                        .padding()
                        .background(Color.accentPrimary)
                        .foregroundColor(.textPrimary)
                        .clipShape(Circle())
                }
            }
        }
        .onAppear { viewModel.loadProjects() }
    }

    private var upgradeBanner: some View {
        Button(action: onUpgrade) {
            HStack {
                Text("Unlock custom trims, full quality & more →")
                    .font(.body).foregroundColor(.premiumAccent)
                Spacer()
            }
            .padding(AppSpacing.md)
            .background(Color.premiumAccent.opacity(0.15))
            .cornerRadius(AppSpacing.cornerButton)
            .padding(.horizontal, AppSpacing.md)
            .padding(.vertical, AppSpacing.sm)
        }
    }

    private var emptyState: some View {
        VStack(spacing: AppSpacing.md) {
            Spacer()
            Text("No projects yet").font(.titleLarge)
            Text("Create your first trim").font(.bodyLarge).foregroundColor(.textSecondary)
            Button("New Project", action: onNewProject).buttonStyle(.borderedProminent).tint(.accentPrimary)
            Spacer()
        }
    }

    private var projectList: some View {
        List {
            ForEach(viewModel.uiState.projects, id: \.id) { project in
                ProjectCard(project: project)
                    .listRowBackground(Color.bgElevated)
                    .swipeActions(edge: .trailing) {
                        Button(role: .destructive) {
                            viewModel.deleteProject(project)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
    }
}

private struct ProjectCard: View {
    let project: ProjectInfo
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: AppSpacing.xs) {
                Text(project.name).font(.bodyLarge).lineLimit(1)
                Text("\(Int(project.mergedDuration))s · \(project.videoCount) videos")
                    .font(.caption).foregroundColor(.textSecondary)
            }
            Spacer()
            Image(systemName: "chevron.right").foregroundColor(.textSecondary)
        }
        .padding(.vertical, AppSpacing.sm)
    }
}
