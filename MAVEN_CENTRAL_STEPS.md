# Maven Central Publication - Next Steps

## What's Been Done ✅

### 1. POM Configuration Updated
- Changed version from `1.0.0-SNAPSHOT` to `1.0.0` (release version)
- Added required Maven Central metadata:
  - Project name, description, and URL
  - Apache License 2.0 declaration
  - Developer information (hoggmania)
  - SCM (GitHub) configuration
  - Issue management (GitHub Issues)
  - Distribution management (Sonatype OSSRH)

### 2. Build Plugins Added
- **maven-source-plugin**: Generates source JAR
- **maven-javadoc-plugin**: Generates Javadoc JAR
- **maven-gpg-plugin**: Signs artifacts with GPG
- **nexus-staging-maven-plugin**: Handles deployment to Sonatype/Maven Central

### 3. Documentation Created
- **LICENSE**: Apache License 2.0 full text
- **PUBLISHING.md**: Complete guide for publishing to Maven Central
- **README.md**: Updated with Maven Central badges and publishing reference

### 4. Changes Committed and Pushed
All changes have been committed and pushed to GitHub.

---

## What You Need to Do Now

### Step 1: Set Up Sonatype Account (One-Time)

1. **Create JIRA Account**
   - Go to: <https://issues.sonatype.org/>
   - Sign up for an account

2. **Request GroupId Access**
   - Create a New Project ticket (OSSRH)
   - Group Id: `io.github.hoggmania`
   - Project URL: `https://github.com/hoggmania/temporal-openapi-generator-maven-plugin`
   - SCM URL: `https://github.com/hoggmania/temporal-openapi-generator-maven-plugin.git`
   
3. **Verify Ownership**
   - They'll ask you to verify GitHub repository ownership
   - This is easy since you own the repository
   - Approval usually takes 1-2 business days

### Step 2: Set Up GPG (One-Time)

1. **Install GPG** (if not already installed)
   ```powershell
   choco install gnupg
   ```

2. **Generate GPG Key**
   ```bash
   gpg --gen-key
   ```
   - Use your name and email
   - Create a strong passphrase (save it!)

3. **Publish Public Key**
   ```bash
   # List your keys to get the key ID
   gpg --list-keys
   
   # Publish to keyservers (replace with your key ID)
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
   ```

### Step 3: Configure Maven Settings

Create/edit `C:\Users\YourName\.m2\settings.xml`:

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

### Step 4: Test Build Locally

Before deploying, test that everything works:

```bash
cd temporal-openapi-generator-maven-plugin
mvn clean verify
```

This will:
- Compile the code
- Run tests
- Generate source JAR
- Generate Javadoc JAR
- Sign all artifacts with GPG

**If this fails**, you need to fix any issues before deploying.

### Step 5: Deploy to Maven Central

Once Sonatype approves your groupId (Step 1) and local build passes (Step 4):

```bash
mvn clean deploy
```

This will:
- Build and sign all artifacts
- Upload to Sonatype staging repository
- Automatically release to Maven Central (autoReleaseAfterClose=true)

### Step 6: Verify Publication

After 10-30 minutes, check Maven Central:
- <https://search.maven.org/artifact/io.github.hoggmania/temporal-openapi-generator-maven-plugin>

After 2-4 hours, users can use:
```xml
<plugin>
    <groupId>io.temporal.openapi</groupId>
    <artifactId>temporal-openapi-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```

### Step 7: Create GitHub Release

1. Go to: <https://github.com/hoggmania/temporal-openapi-generator-maven-plugin/releases>
2. Click "Create a new release"
3. Tag: `v1.0.0`
4. Title: `Release 1.0.0`
5. Description: Copy from release notes below
6. Publish release

---

## Release Notes Template

```markdown
# Release 1.0.0

First stable release of Temporal OpenAPI Generator Maven Plugin!

## Features

✅ Generate Temporal Activity interfaces from OpenAPI specifications
✅ Generate Activity implementations that delegate to OpenAPI Generator clients
✅ Type-safe Java code with proper type mapping
✅ Automatic idempotency detection (GET, PUT, DELETE)
✅ Proper error handling with ApplicationFailure
✅ Support for complex request/response models
✅ Configurable retry policies based on HTTP methods
✅ Full Maven lifecycle integration

## Installation

Add to your `pom.xml`:

```xml
<plugin>
    <groupId>io.github.hoggmania</groupId>
    <artifactId>temporal-openapi-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
            <configuration>
                <specFile>${project.basedir}/src/main/resources/openapi.yaml</specFile>
                <packageName>com.example.temporal.activities</packageName>
                <activityName>ApiActivity</activityName>
                <apiClientPackage>com.example.api.client</apiClientPackage>
            </configuration>
        </execution>
    </executions>
</plugin>
\`\`\`

## Documentation

- [README](https://github.com/hoggmania/temporal-openapi-generator-maven-plugin#readme)
- [Quick Start Guide](https://github.com/hoggmania/temporal-openapi-generator-maven-plugin/blob/main/QUICKSTART.md)
- [Example Project](https://github.com/hoggmania/temporal-openapi-generator-maven-plugin/tree/main/example)

## Requirements

- Java 11+
- Maven 3.6+
- Temporal SDK 1.20.1+
- OpenAPI Specification 3.0+
```

---

## Timeline

**Estimated Total Time: 1-3 days**

1. **Day 0**: Setup (this is done!)
   - ✅ POM configuration
   - ✅ Build plugins
   - ✅ Documentation
   - ✅ LICENSE file

2. **Day 1**: Sonatype Setup
   - Create JIRA account
   - Request groupId access
   - Wait for approval (1-2 business days)

3. **Day 2-3**: Publish
   - Configure GPG
   - Configure Maven settings
   - Test local build
   - Deploy to Maven Central
   - Create GitHub release

---

## Troubleshooting

If you encounter issues, see [PUBLISHING.md](PUBLISHING.md) for detailed troubleshooting steps.

Common issues:
- GPG signing errors → Check GPG configuration
- Authentication errors → Verify Sonatype credentials
- Validation errors → Check POM metadata is complete
- Build failures → Run `mvn clean verify` locally first

---

## Support

For questions or issues:
- GitHub Issues: <https://github.com/hoggmania/temporal-openapi-generator-maven-plugin/issues>
- Sonatype Support: <https://central.sonatype.org/support>
