# Publishing to Maven Central

This guide explains how to publish the Temporal OpenAPI Generator Maven Plugin to Maven Central.

## Prerequisites

### 1. Create Sonatype JIRA Account

1. Go to https://issues.sonatype.org/
2. Create an account
3. Create a New Project ticket (Central) to claim your `io.github.hoggmania` groupId
   - Project: Community Support - Open Source Project Repository Hosting (Central)
   - Issue Type: New Project
   - Group Id: `io.github.hoggmania`
   - Project URL: `https://github.com/hoggmania/temporal-openapi-generator-maven-plugin`
   - SCM URL: `https://github.com/hoggmania/temporal-openapi-generator-maven-plugin.git`
4. Wait for approval (usually takes 1-2 business days)
5. You'll need to verify domain ownership or GitHub repository ownership

### 2. Install and Configure GPG

#### Install GPG

**Windows (via Chocolatey):**
```powershell
choco install gnupg
```

**Or download from:** https://www.gnupg.org/download/

#### Generate GPG Key

```bash
gpg --gen-key
```

Follow the prompts:
- Real name: Your name
- Email: Your email
- Passphrase: Choose a strong passphrase

#### List your keys

```bash
gpg --list-keys
```

Output will show something like:
```
pub   rsa3072 2025-11-27 [SC] [expires: 2027-11-27]
      ABCDEF1234567890ABCDEF1234567890ABCDEF12
uid           [ultimate] Your Name <your.email@example.com>
sub   rsa3072 2025-11-27 [E] [expires: 2027-11-27]
```

The long hex string is your key ID.

#### Publish your public key

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys ABCDEF1234567890ABCDEF1234567890ABCDEF12
```

Also publish to other keyservers:
```bash
gpg --keyserver keys.openpgp.org --send-keys ABCDEF1234567890ABCDEF1234567890ABCDEF12
gpg --keyserver pgp.mit.edu --send-keys ABCDEF1234567890ABCDEF1234567890ABCDEF12
```

### 3. Configure Maven Settings

Edit or create `~/.m2/settings.xml` (Windows: `C:\Users\YourName\.m2\settings.xml`):

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_MAVEN_CENTRAL_TOKEN_USERNAME</username>
      <password>YOUR_MAVEN_CENTRAL_TOKEN_PASSWORD</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>release</id>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

**Note:** This project uses the `central-publishing-maven-plugin` which requires Maven Central Portal credentials:
1. Go to https://central.sonatype.com/ and sign in
2. Click your username → View Account → Generate User Token
3. Use the generated token username and password in your settings.xml

**Security Note:** For production, use encrypted passwords:
```bash
mvn --encrypt-master-password
mvn --encrypt-password
```

## Publishing Steps

### 1. Verify the Build

```bash
mvn clean verify
```

This will:
- Compile the code
- Run tests
- Generate source JAR
- Generate Javadoc JAR
- Sign all artifacts with GPG

### 2. Deploy to Maven Central

```bash
mvn clean deploy -P release
```

The `central-publishing-maven-plugin` will automatically:
1. Build and sign all artifacts (sources, javadoc, main JAR)
2. Upload to Maven Central Portal
3. Validate the deployment
4. Publish to Maven Central

### 3. Monitor Deployment Status

You can check the deployment status at:
- Maven Central Portal: https://central.sonatype.com/publishing
- View your published artifacts after processing completes

**Note:** The `central-publishing-maven-plugin` publishes directly to Maven Central Portal, which is the new simplified publishing process. The old Nexus OSSRH staging workflow is no longer needed for new projects using the `io.github.*` groupId namespace.

### 4. Verify Publication

After 10-30 minutes, check Maven Central:
- https://search.maven.org/artifact/io.github.hoggmania/temporal-openapi-generator-maven-plugin

After 2-4 hours, it will be available via:
```xml
<dependency>
    <groupId>io.github.hoggmania</groupId>
    <artifactId>temporal-openapi-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Troubleshooting

### GPG Signing Fails

**Error:** `gpg: signing failed: Inappropriate ioctl for device`

**Solution:**
```bash
export GPG_TTY=$(tty)
```

Or add to your `~/.bashrc` or `~/.zshrc`

**Windows PowerShell:**
Set environment variable in Maven settings.xml or use:
```powershell
$env:GPG_TTY = "not-needed"
```

### GPG Cannot Find Key

Make sure GPG can find your key:
```bash
gpg --list-secret-keys
```

### Deployment Fails - Unauthorized

Verify your Maven Central Portal credentials in `~/.m2/settings.xml`:
- Make sure you're using a **user token** from https://central.sonatype.com/ (not your login password)
- The server `<id>` must be `central` to match the plugin configuration
- Generate a new token if your current one has expired

### Validation Errors

Common issues:
- Missing or invalid POM metadata (licenses, developers, SCM)
- Missing source or javadoc JARs
- Unsigned artifacts
- Invalid group ID (not approved by Sonatype)

Check staging repository activity tab for specific errors.

## Release Checklist

Before releasing a new version:

- [ ] Update version in `pom.xml` (remove -SNAPSHOT)
- [ ] Update CHANGELOG.md
- [ ] Update documentation (README.md, etc.)
- [ ] Run full test suite: `mvn clean verify`
- [ ] Build example: `cd example && mvn clean package`
- [ ] Test plugin locally in a separate project
- [ ] Commit and push all changes
- [ ] Create Git tag: `git tag -a v1.0.0 -m "Release 1.0.0"`
- [ ] Push tag: `git push origin v1.0.0`
- [ ] Deploy: `mvn clean deploy`
- [ ] Create GitHub release with release notes
- [ ] Verify artifact on Maven Central
- [ ] Update version to next SNAPSHOT (e.g., 1.1.0-SNAPSHOT)
- [ ] Announce release

## Post-Release

1. **Update Documentation:** Update README badges, version numbers
2. **Announce:** Post on relevant forums, Twitter, etc.
3. **Monitor:** Watch for issues on GitHub
4. **Plan Next Release:** Create milestone for next version

## Useful Commands

```bash
# Check GPG configuration
gpg --version
gpg --list-keys
gpg --list-secret-keys

# Test signing
gpg --sign --detach-sign test.txt

# View POM that will be deployed
mvn help:effective-pom

# Dry-run deployment
mvn clean deploy -DskipTests -DaltDeploymentRepository=local::file:./target/staging

# Deploy specific version
mvn versions:set -DnewVersion=1.0.0
mvn clean deploy
mvn versions:commit  # or versions:revert if you want to undo
```

## Additional Resources

- [Maven Central Portal Publishing Guide](https://central.sonatype.org/publish/publish-portal-maven/)
- [Central Publishing Maven Plugin Documentation](https://central.sonatype.org/publish/publish-portal-maven/)
- [Maven Central Portal](https://central.sonatype.com/)
- [Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)
- [GitHub Publishing to Maven Central](https://central.sonatype.org/publish/publish-portal-github/)
