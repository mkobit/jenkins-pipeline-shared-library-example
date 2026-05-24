package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

// Holds only a plain String — no script reference needed.
class NotificationPayload implements Serializable {

    private final String template

    NotificationPayload(final String template) {
        this.template = Objects.requireNonNull(template)
    }

    // Chained String.replace on GString-derived values is CPS-unsafe.
    @NonCPS
    String render(BuildContext ctx, String status) {
        template
                .replace('{{job}}',    ctx.jobName)
                .replace('{{build}}',  String.valueOf(ctx.buildNumber))
                .replace('{{branch}}', ctx.branch)
                .replace('{{status}}', status)
    }
}
