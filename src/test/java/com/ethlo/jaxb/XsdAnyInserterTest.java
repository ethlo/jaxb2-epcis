package com.ethlo.jaxb;

import com.ethlo.util.XsdAnyInserter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class XsdAnyInserterTest {

    @TempDir
    Path tempDir;

    @Test
    void testProcess_modifiesBaseButSkipsChild() throws Exception {
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(inputDir);

        // Create a base schema
        String baseSchemaContent = """
                <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                            targetNamespace="urn:example:base"
                            xmlns:base="urn:example:base"
                            elementFormDefault="qualified">
                    <xsd:complexType name="BaseType">
                        <xsd:sequence>
                            <xsd:element name="baseField" type="xsd:string"/>
                        </xsd:sequence>
                    </xsd:complexType>
                </xsd:schema>
                """;
        Files.writeString(inputDir.resolve("Base.xsd"), baseSchemaContent);

        // Create a child schema that imports and extends the base
        String childSchemaContent = """
                <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                            targetNamespace="urn:example:child"
                            xmlns:child="urn:example:child"
                            xmlns:base="urn:example:base"
                            elementFormDefault="qualified">
                    <xsd:import namespace="urn:example:base" schemaLocation="Base.xsd"/>
                    <xsd:complexType name="ChildType">
                        <xsd:complexContent>
                            <xsd:extension base="base:BaseType">
                                <xsd:sequence>
                                    <xsd:element name="childField" type="xsd:string"/>
                                </xsd:sequence>
                            </xsd:extension>
                        </xsd:complexContent>
                    </xsd:complexType>
                </xsd:schema>
                """;
        Files.writeString(inputDir.resolve("Child.xsd"), childSchemaContent);

        // 2. Execute: Run the tool
        String[] args = {inputDir.toString(), outputDir.toString()};
        XsdAnyInserter.main(args);

        // 3. Assert: Check the results
        Path modifiedBasePath = outputDir.resolve("Base.xsd");
        Path modifiedChildPath = outputDir.resolve("Child.xsd");

        // Assert that the base schema was modified correctly
        assertThat(modifiedBasePath).exists();
        String modifiedBaseContent = Files.readString(modifiedBasePath);
        assertThat(modifiedBaseContent)
                .doesNotContain("<xs:any")
                .doesNotContain("<xs:anyAttribute");

        // Assert that the child schema was NOT modified, because it inherits 'any'
        assertThat(modifiedChildPath).exists();
        String modifiedChildContent = Files.readString(modifiedChildPath);
        assertThat(modifiedChildContent)
                .contains("<xs:any")
                .contains("<xs:anyAttribute");
    }

    @Test
    void testDirectory() throws Exception {
        Path inputDir = Path.of("src/main/resources");
        Path outputDir = tempDir;

        String[] args = {inputDir.toString(), outputDir.toString()};
        XsdAnyInserter.main(args);
    }
}