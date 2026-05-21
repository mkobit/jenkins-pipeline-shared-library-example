package com.mkobit.libraryexample.config

import com.cloudbees.groovy.cps.NonCPS

/** Site-to-region mapping shared across multiple Jenkins shared libraries. */
class SiteConfig implements Serializable {

    @NonCPS
    static String region(String site) {
        switch (site) {
            case 'us-east':
                return 'us-east-1'
            case 'eu-west':
                return 'eu-west-1'
            default:
                return 'unknown'
        }
    }
}
