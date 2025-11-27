# Publishing to Maven Central

This guide explains how to publish the Temporal OpenAPI Generator Maven Plugin to Maven Central.

## Prerequisites

### 1. Create Sonatype JIRA Account

1. Go to https://issues.sonatype.org/
2. Create an account
3. Create a New Project ticket (OSSRH) to claim your `io.temporal.openapi` groupId
   - Project: Community Support - Open Source Project Repository Hosting (OSSRH)
   - Issue Type: New Project
   - Group Id: `io.temporal.openapi`
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
      <id>ossrh</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

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

### 2. Deploy to Staging

```bash
mvn clean deploy -P release
```

Or if you're ready to release directly:

```bash
mvn clean deploy
```

The Nexus Staging plugin is configured with `autoReleaseAfterClose=true`, so it will automatically:
1. Upload to staging repository
2. Close the staging repository
3. Run validation rules
4. Release to Maven Central (if validation passes)

### 3. Manual Staging (Alternative)

If you prefer manual control, update `pom.xml` to set `autoReleaseAfterClose=false`, then:

```bash
# Deploy to staging
mvn clean deploy

# Login to Sonatype
# https://s01.oss.sonatype.org/

# Navigate to "Staging Repositories"
# Find your repository (iotemporal-xxxx)
# Click "Close" to validate
# Click "Release" to publish to Maven Central
```

### 4. Verify Publication

After 10-30 minutes, check Maven Central:
- https://search.maven.org/artifact/io.temporal.openapi/temporal-openapi-generator-maven-plugin

After 2-4 hours, it will be available via:
```xml
<dependency>
    <groupId>io.temporal.openapi</groupId>
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

Verify your Sonatype credentials in `~/.m2/settings.xml`

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

- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)
- [Nexus Staging Plugin](https://github.com/sonatype/nexus-maven-plugins/tree/main/staging/maven-plugin)
