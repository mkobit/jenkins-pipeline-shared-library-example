def call(String label) {
    currentBuild.displayName = "#${currentBuild.number} ${label}"
    currentBuild.description = label
}
