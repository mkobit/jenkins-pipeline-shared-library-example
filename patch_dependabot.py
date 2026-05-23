with open('.github/dependabot.yml', 'r') as f:
    content = f.read()

new_block = """  - package-ecosystem: "gradle"
    target-branch: "main"
    directory: "/"
    schedule:
      interval: "weekly"
    labels:
      - "infra"
    cooldown:
      default-days: 60
    allow:
      - dependency-name: "io.jenkins:*"
      - dependency-name: "io.jenkins.*:*"
      - dependency-name: "com.jenkins:*"
      - dependency-name: "com.jenkins.*:*"
      - dependency-name: "org.6wind.jenkins:*"
      - dependency-name: "org.6wind.jenkins.*:*"
      - dependency-name: "org.jenkins-ci.plugins:*"
      - dependency-name: "org.jenkins-ci.plugins.*:*"
      - dependency-name: "org.jenkinsci.plugins:*"
      - dependency-name: "org.jenkinsci.plugins.*:*"
      - dependency-name: "org.jenkins-ci.modules:*"
      - dependency-name: "org.jenkins-ci.modules.*:*"
    groups:
      jenkins-plugins:
        patterns:
          - "io.jenkins:*"
          - "io.jenkins.*:*"
          - "com.jenkins:*"
          - "com.jenkins.*:*"
          - "org.6wind.jenkins:*"
          - "org.6wind.jenkins.*:*"
          - "org.jenkins-ci.plugins:*"
          - "org.jenkins-ci.plugins.*:*"
          - "org.jenkinsci.plugins:*"
          - "org.jenkinsci.plugins.*:*"
          - "org.jenkins-ci.modules:*"
          - "org.jenkins-ci.modules.*:*"

"""

ignore_addition = """    ignore:
      - dependency-name: "io.jenkins:*"
      - dependency-name: "io.jenkins.*:*"
      - dependency-name: "com.jenkins:*"
      - dependency-name: "com.jenkins.*:*"
      - dependency-name: "org.6wind.jenkins:*"
      - dependency-name: "org.6wind.jenkins.*:*"
      - dependency-name: "org.jenkins-ci.plugins:*"
      - dependency-name: "org.jenkins-ci.plugins.*:*"
      - dependency-name: "org.jenkinsci.plugins:*"
      - dependency-name: "org.jenkinsci.plugins.*:*"
      - dependency-name: "org.jenkins-ci.modules:*"
      - dependency-name: "org.jenkins-ci.modules.*:*"
"""
if "jenkins-plugins" not in content:
    content = content.replace("    ignore:", ignore_addition)
    content = content.replace('updates:\n', 'updates:\n' + new_block)

    with open('.github/dependabot.yml', 'w') as f:
        f.write(content)
