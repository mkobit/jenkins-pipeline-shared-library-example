/** Demo step from :base-lib — proves :base-lib reaches Jenkins via :config-lib's transitive declaration. */
def call(String message) {
    echo "[base-lib] ${message}"
}
