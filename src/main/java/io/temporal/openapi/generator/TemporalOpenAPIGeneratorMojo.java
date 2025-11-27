package io.temporal.openapi.generator;

import com.squareup.javapoet.JavaFile;
import io.temporal.openapi.generator.codegen.ActivityInterfaceGenerator;
import io.temporal.openapi.generator.codegen.ActivityImplementationGenerator;
import io.temporal.openapi.generator.codegen.ModelGenerator;
import io.temporal.openapi.generator.model.OperationModel;
import io.temporal.openapi.generator.parser.OpenAPIParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Maven plugin to generate Temporal Activities from OpenAPI specifications
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class TemporalOpenAPIGeneratorMojo extends AbstractMojo {

    /**
     * Location of the OpenAPI specification file (YAML or JSON)
     */
    @Parameter(property = "openapi.spec", required = true)
    private File specFile;

    /**
     * Output directory for generated source files
     */
    @Parameter(
        property = "openapi.outputDirectory",
        defaultValue = "${project.build.directory}/generated-sources/openapi"
    )
    private File outputDirectory;

    /**
     * Base package name for generated classes
     */
    @Parameter(property = "openapi.packageName", defaultValue = "io.temporal.openapi.generated")
    private String packageName;

    /**
     * Name of the generated Activity interface
     */
    @Parameter(property = "openapi.activityName", defaultValue = "ApiActivity")
    private String activityName;

    /**
     * Package name where OpenAPI Generator client classes are located
     */
    @Parameter(
        property = "openapi.apiClientPackage", 
        defaultValue = "io.temporal.openapi.generated.client"
    )
    private String apiClientPackage;

    /**
     * Package name where OpenAPI Generator model classes are located
     */
    @Parameter(
        property = "openapi.modelPackage",
        defaultValue = "io.temporal.openapi.generated.models"
    )
    private String modelPackage;

    /**
     * Whether to generate implementation class
     */
    @Parameter(property = "openapi.generateImplementation", defaultValue = "true")
    private boolean generateImplementation;

    /**
     * Whether to generate model POJOs
     */
    @Parameter(property = "openapi.generateModels", defaultValue = "true")
    private boolean generateModels;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting Temporal OpenAPI Generator...");
        getLog().info("OpenAPI Spec: " + specFile.getAbsolutePath());
        getLog().info("Output Directory: " + outputDirectory.getAbsolutePath());
        getLog().info("Package Name: " + packageName);
        getLog().info("Activity Name: " + activityName);

        // Validate inputs
        if (!specFile.exists()) {
            throw new MojoExecutionException("OpenAPI spec file not found: " + specFile.getAbsolutePath());
        }

        // Create output directory
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        try {
            // Parse OpenAPI specification
            getLog().info("Parsing OpenAPI specification...");
            OpenAPIParser parser = new OpenAPIParser(specFile.getAbsolutePath(), modelPackage);
            List<OperationModel> operations = parser.parseOperations();
            getLog().info("Found " + operations.size() + " operations");

            // Generate models if requested
            if (generateModels) {
                getLog().info("Generating model POJOs...");
                ModelGenerator modelGenerator = new ModelGenerator(
                    parser.getTypeMapper(), 
                    packageName + ".models"
                );
                List<JavaFile> modelFiles = modelGenerator.generateModels();
                
                for (JavaFile javaFile : modelFiles) {
                    Path outputPath = outputDirectory.toPath();
                    javaFile.writeTo(outputPath);
                }
                getLog().info("Generated " + modelFiles.size() + " model classes");
            }

            // Generate Activity interface
            getLog().info("Generating Activity interface...");
            ActivityInterfaceGenerator interfaceGenerator = new ActivityInterfaceGenerator(
                parser.getTypeMapper(),
                packageName,
                activityName
            );
            JavaFile interfaceFile = interfaceGenerator.generateActivityInterface(operations);
            interfaceFile.writeTo(outputDirectory.toPath());
            getLog().info("Generated Activity interface: " + activityName);

            // Generate implementation if requested
            if (generateImplementation) {
                getLog().info("Generating Activity implementation...");
                ActivityImplementationGenerator implGenerator = new ActivityImplementationGenerator(
                    packageName,
                    activityName,
                    apiClientPackage
                );
                JavaFile implFile = implGenerator.generateImplementation(operations);
                implFile.writeTo(outputDirectory.toPath());
                getLog().info("Generated Activity implementation: " + activityName + "Impl");
            }

            // Add generated sources to Maven project
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
            getLog().info("Added generated sources to compile source root");

            getLog().info("Temporal OpenAPI Generator completed successfully!");

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write generated files", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate Temporal Activities", e);
        }
    }
}
