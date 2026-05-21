/** Returns the canonical region for a logical site name (`us-east`, `eu-west`, …). */
def call(String site) {
    return com.mkobit.libraryexample.config.SiteConfig.region(site)
}
