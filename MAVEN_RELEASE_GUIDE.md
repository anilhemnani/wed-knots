╔════════════════════════════════════════════════════════════════════════════════╗
║                                                                                ║
║                 ✅ MAVEN RELEASE CONFIGURATION - COMPLETE                     ║
║                                                                                ║
║              SCM and Release Plugin setup for automated versioning             ║
║                                                                                ║
╚════════════════════════════════════════════════════════════════════════════════╝


📋 CONFIGURATION ADDED TO pom.xml
═════════════════════════════════════════════════════════════════════════════════

✅ SCM (Source Control Management) Block
──────────────────────────────────────────
<scm>
    <connection>scm:git:https://github.com/yourusername/wed-knots.git</connection>
    <developerConnection>scm:git:https://github.com/yourusername/wed-knots.git</developerConnection>
    <url>https://github.com/yourusername/wed-knots</url>
    <tag>HEAD</tag>
</scm>

Purpose:
  • Defines source control location for Maven
  • Used by maven-release-plugin for tagging
  • Supports Git, SVN, and other SCMs
  • connection: Read-only connection (for CI/CD)
  • developerConnection: Read-write connection (for developers)


✅ Distribution Management Block
─────────────────────────────────
<distributionManagement>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/yourusername/wed-knots</url>
    </repository>
    <snapshotRepository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/yourusername/wed-knots</url>
    </snapshotRepository>
</distributionManagement>

Purpose:
  • Defines where artifacts are deployed
  • GitHub Packages is configured as target repository
  • Separate repositories for releases and snapshots
  • Can be changed to Nexus, Artifactory, etc.


✅ Maven Release Plugin
───────────────────────
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-release-plugin</artifactId>
    <version>3.0.1</version>
    <configuration>
        <autoVersionSubmodules>true</autoVersionSubmodules>
        <useReleaseProfile>true</useReleaseProfile>
        <releaseProfiles>release</releaseProfiles>
        <goals>deploy</goals>
        <scmCommentPrefix>[maven-release-plugin]</scmCommentPrefix>
        <tagNameFormat>v@{project.version}</tagNameFormat>
        <preparationGoals>clean verify</preparationGoals>
    </configuration>
</plugin>

Configuration Options:
  • autoVersionSubmodules: Update all modules with same version
  • useReleaseProfile: Use release profile for building
  • goals: Deploy artifacts after release
  • tagNameFormat: Git tag format (v1.0.0)
  • preparationGoals: Goals to run before release (clean verify)


✅ Maven SCM Plugin
────────────────────
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-scm-plugin</artifactId>
    <version>2.0.0</version>
    <configuration>
        <connectionType>developerConnection</connectionType>
    </configuration>
</plugin>

Purpose:
  • Handles source control operations
  • Used for checkout, commit, tag operations
  • Uses developerConnection for read-write access


✅ JAR Plugin Configuration
─────────────────────────────
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <archive>
            <manifest>
                <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
            </manifest>
        </archive>
    </configuration>
</plugin>

Purpose:
  • Adds version information to JAR manifest
  • Makes version queryable at runtime
  • Helps track which version is deployed


═════════════════════════════════════════════════════════════════════════════════

🔧 SETUP REQUIREMENTS
═════════════════════════════════════════════════════════════════════════════════

BEFORE USING MAVEN RELEASE:

1. GITHUB CONFIGURATION
──────────────────────
   • Repository must be on GitHub (or configure appropriate SCM)
   • Update SCM URLs with your actual GitHub username
   
   Replace: yourusername/wed-knots
   With: your-actual-github-username/wed-knots

2. GITHUB PERSONAL ACCESS TOKEN
───────────────────────────────
   Create a Personal Access Token:
   1. Go to GitHub Settings → Developer settings → Personal access tokens
   2. Click "Generate new token"
   3. Select scopes:
      ✓ repo (full control of private repositories)
      ✓ read:packages (read packages from GitHub Packages)
      ✓ write:packages (publish packages to GitHub Packages)
   4. Copy the token (use it for authentication)

3. MAVEN SETTINGS (~/.m2/settings.xml)
──────────────────────────────────────
   Add GitHub Packages credentials:

   <servers>
       <server>
           <id>github</id>
           <username>YOUR_GITHUB_USERNAME</username>
           <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
       </server>
   </servers>

4. GIT CONFIGURATION
────────────────────
   Configure Git user for commits:
   
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"

5. LOCAL SETUP
───────────────
   • Ensure you're on the master/main branch
   • All changes must be committed
   • No uncommitted changes allowed
   • Remote repository must be up to date


═════════════════════════════════════════════════════════════════════════════════

🚀 MAVEN RELEASE WORKFLOW
═════════════════════════════════════════════════════════════════════════════════

STEP 1: PREPARE RELEASE
────────────────────────
mvn release:prepare

What it does:
  1. Validates project (no uncommitted changes)
  2. Checks for SNAPSHOT versions
  3. Interactive prompts:
     • Release version (e.g., 1.0.0)
     • Git tag name (e.g., v1.0.0)
     • Next development version (e.g., 1.0.1-SNAPSHOT)
  4. Updates pom.xml with release version
  5. Runs: clean verify
  6. Creates Git commit with updated pom.xml
  7. Creates Git tag
  8. Updates pom.xml to next SNAPSHOT version
  9. Creates second Git commit

Output:
  • Git commits created
  • Git tag created (NOT pushed yet)
  • pom.xml modified locally


STEP 2: PERFORM RELEASE
────────────────────────
mvn release:perform

What it does:
  1. Checks out release tag from Git
  2. Builds the release JAR
  3. Runs: clean verify
  4. Deploys artifacts to GitHub Packages
  5. Pushes commits and tags to remote
  6. Cleans up release-pom.xml file

Output:
  • Artifacts deployed to GitHub Packages
  • Git changes pushed to remote
  • Version released!


STEP 3: VERIFY RELEASE
───────────────────────
Check GitHub:
  • Tags: github.com/yourusername/wed-knots/releases
  • Packages: github.com/yourusername/wed-knots/packages

Check Maven:
  • Deployed JAR should be available in GitHub Packages


═════════════════════════════════════════════════════════════════════════════════

📌 COMMON RELEASE SCENARIOS
═════════════════════════════════════════════════════════════════════════════════

SCENARIO 1: STANDARD RELEASE
────────────────────────────
Current version: 0.0.1-SNAPSHOT

mvn release:prepare
  → Release version: 0.0.1
  → Tag: v0.0.1
  → Next version: 0.0.2-SNAPSHOT

mvn release:perform
  → Deploys 0.0.1 JAR to GitHub Packages
  → Updates main branch to 0.0.2-SNAPSHOT


SCENARIO 2: ROLLBACK IF SOMETHING GOES WRONG
───────────────────────────────────────────
mvn release:rollback

What it does:
  • Reverts pom.xml to pre-release state
  • Removes commits (NOT tags)
  • Allows retry


SCENARIO 3: AUTOMATED RELEASE (CI/CD)
──────────────────────────────────────
In GitHub Actions or Jenkins:

jobs:
  release:
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Release
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          git config --global user.name "GitHub Actions"
          git config --global user.email "actions@github.com"
          mvn release:prepare release:perform


═════════════════════════════════════════════════════════════════════════════════

🔑 KEY POINTS
═════════════════════════════════════════════════════════════════════════════════

SCM Block:
  ✓ Defines Git repository location
  ✓ Used by maven-release-plugin
  ✓ Must match actual repository URL
  ✓ Supports scm:git:, scm:svn:, etc.

Distribution Management:
  ✓ Defines where artifacts go
  ✓ GitHub Packages is configured
  ✓ Can be Nexus, Artifactory, Maven Central
  ✓ Separate URLs for releases and snapshots

Release Plugin:
  ✓ Handles version bumping
  ✓ Creates Git tags
  ✓ Automates deployment
  ✓ Interactive release process

Version Format:
  ✓ Releases: 1.0.0 (remove -SNAPSHOT)
  ✓ Snapshots: 1.0.1-SNAPSHOT
  ✓ Git tags: v1.0.0 (with v prefix)


═════════════════════════════════════════════════════════════════════════════════

⚠️ BEFORE YOUR FIRST RELEASE
═════════════════════════════════════════════════════════════════════════════════

CHECKLIST:

□ Update GitHub URLs in pom.xml
  Replace: yourusername/wed-knots
  With: actual-github-username/wed-knots

□ Push all code to GitHub
  git push origin main

□ Ensure all tests pass
  mvn clean verify

□ Create GitHub Personal Access Token
  • Settings → Developer settings → Personal access tokens
  • Scopes: repo, read:packages, write:packages

□ Configure Maven settings
  ~/.m2/settings.xml with GitHub credentials

□ Configure Git
  git config --global user.name "Your Name"
  git config --global user.email "your.email"

□ Create a release profile (optional)
  For custom release build configuration


═════════════════════════════════════════════════════════════════════════════════

📝 EXAMPLE RELEASE PROFILE (OPTIONAL)
═════════════════════════════════════════════════════════════════════════════════

Add to pom.xml in <profiles> section (if not present, create it):

<profiles>
    <profile>
        <id>release</id>
        <build>
            <plugins>
                <!-- Sign artifacts with GPG (optional but recommended) -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-gpg-plugin</artifactId>
                    <version>3.0.1</version>
                    <executions>
                        <execution>
                            <id>sign-artifacts</id>
                            <phase>verify</phase>
                            <goals>
                                <goal>sign</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>


═════════════════════════════════════════════════════════════════════════════════

🎯 WHAT TO DO NOW
═════════════════════════════════════════════════════════════════════════════════

IMMEDIATE ACTIONS:

1. Update GitHub URLs in pom.xml
   Replace: yourusername/wed-knots
   With: your-actual-github-username/wed-knots

2. Create GitHub Personal Access Token
   • Go to GitHub Settings → Developer settings → Personal access tokens
   • Select scopes: repo, read:packages, write:packages
   • Copy token

3. Configure ~/.m2/settings.xml
   Add server block with GitHub credentials:
   
   <server>
       <id>github</id>
       <username>YOUR_GITHUB_USERNAME</username>
       <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
   </server>

4. Configure Git (if not already done)
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"

5. Commit and push all changes
   git add -A
   git commit -m "Add Maven release and SCM configuration"
   git push origin main

6. Test release process (optional)
   mvn release:prepare --dry-run
   This simulates the release without committing


═════════════════════════════════════════════════════════════════════════════════

🚀 PERFORM YOUR FIRST RELEASE
═════════════════════════════════════════════════════════════════════════════════

When ready to release version 1.0.0:

1. Ensure all tests pass:
   mvn clean verify

2. Prepare release:
   mvn release:prepare
   
   When prompted:
   • Release version: 1.0.0
   • Tag: v1.0.0
   • Next version: 1.0.1-SNAPSHOT

3. Perform release:
   mvn release:perform

4. Verify on GitHub:
   • Check tags: github.com/yourusername/wed-knots/releases
   • Check packages: github.com/yourusername/wed-knots/packages

Done! Your application is released and versioned!


═════════════════════════════════════════════════════════════════════════════════

📚 RESOURCES
═════════════════════════════════════════════════════════════════════════════════

Official Documentation:
  • Maven Release Plugin: https://maven.apache.org/maven-release/maven-release-plugin/
  • Maven SCM Plugin: https://maven.apache.org/scm/maven-scm-plugin/
  • GitHub Packages: https://docs.github.com/en/packages

Guides:
  • Maven Release Process: https://maven.apache.org/guides/mini/guide-releasing.html
  • GitHub Packages Maven: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry


═════════════════════════════════════════════════════════════════════════════════

Created: January 12, 2026
Application: WedKnots - Wedding Management System
Status: ✅ MAVEN RELEASE READY

