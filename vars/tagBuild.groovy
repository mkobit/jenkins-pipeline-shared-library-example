/**
 * Sets the build's display name and description.
 * @param label the label to set
 */
def call(String label) {
    currentBuild.displayName = "#${currentBuild.number} ${label}"
    currentBuild.description = label
}
