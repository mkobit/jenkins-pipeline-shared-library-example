FROM jenkins/jenkins:2.568-jdk21

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

HEALTHCHECK --interval=10s --timeout=5s --start-period=180s --retries=6 \
  CMD curl -sf http://localhost:8080/login || exit 1
