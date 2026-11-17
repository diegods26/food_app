package br.com.diegopimenta.food_app.iam;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.diegopimenta.food_app.iam", importOptions = ImportOption.DoNotIncludeTests.class )
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that()
            .resideInAPackage("..iam.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "com.fassterxml..",
                    "..iam.application..",
                    "..iam.infrastructure.."
            );

    @ArchTest
    static final ArchRule applicationDoesNotKnowInfrastructure = noClasses()
            .that()
            .resideInAPackage("..iam.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..iam.infrastructure..");

}
