# Example Projects

This directory contains example projects demonstrating the Temporal OpenAPI Generator Maven Plugin.

## Projects

### [example](./example/)
Pet Store API example using a local OpenAPI specification file.

### [example-aquasec](./example-aquasec/)
Aquasec API example that downloads the OpenAPI specification from a remote URL at build time.

## Running the Examples

Each example has its own README with specific instructions. Generally:

1. **Install the plugin** (from repository root):
   ```bash
   mvn clean install
   ```

2. **Build the example**:
   ```bash
   cd example
   mvn clean compile
   ```

3. **Explore generated code**:
   - OpenAPI client: `target/generated-sources/openapi-client/`
   - Temporal activities: `target/generated-sources/temporal-activities/`
