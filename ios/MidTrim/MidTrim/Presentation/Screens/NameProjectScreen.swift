import SwiftUI

struct NameProjectScreen: View {
    let defaultName: String
    let onSave: (String) -> Void
    let onDiscard: () -> Void
    @State private var name: String = ""

    var body: some View {
        VStack(spacing: AppSpacing.lg) {
            Spacer()
            TextField("Project Name", text: $name)
                .textFieldStyle(.roundedBorder).font(.bodyLarge)
                .padding(.horizontal, AppSpacing.md)
            Button("Save") { onSave(name) }
                .buttonStyle(.borderedProminent).tint(.accentPrimary)
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                .padding(.horizontal, AppSpacing.md)
            Spacer()
        }
        .background(Color.bgPrimary)
        .navigationTitle("Save Project")
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("Discard", action: onDiscard)
            }
        }
        .onAppear { name = defaultName }
    }
}
