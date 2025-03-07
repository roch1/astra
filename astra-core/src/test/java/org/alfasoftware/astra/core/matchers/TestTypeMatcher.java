package org.alfasoftware.astra.core.matchers;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.alfasoftware.astra.core.refactoring.UseCase;
import org.alfasoftware.astra.core.utils.AstraUtils;
import org.alfasoftware.astra.core.utils.ClassVisitor;
import org.alfasoftware.astra.exampleTypes.B;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

public class TestTypeMatcher {

  protected static final String TEST_SOURCE = Paths.get(".").toAbsolutePath().normalize().toString().concat("/src/test/java");

  public static final String SIMPLE_INTERFACE = "package x; public interface Test {}";
  public static final String SIMPLE_CLASS = "package x; public class Test {}";
  public static final String CLASS_IMPLEMENTS_INTERFACE =
          "package x;\r\n" +
                  " import org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface;\r\n" +
                  " public class Test implements ExampleMarkerInterface {}\r\n";
  public static final String CLASS_IMPLEMENTS_QUALIFIED_INTERFACE =
          "package x;\r\n" +
                  " public class Test implements org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface {}\r\n";

  public static final String CLASS_IMPLEMENTS_INTERFACE_AND_EXTENDS_CLASS =
          "package x;\r\n" +
                  " import org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface;\r\n" +
                  " import org.alfasoftware.astra.exampleTypes.B;\r\n" +
                  " public class Test extends B implements ExampleMarkerInterface {}\r\n";

  
  @Test
  public void testTypeMatcherForInterface() {
    Matcher matcher = TypeMatcher.builder().asInterface().build();
    ClassVisitor visitor = parse(SIMPLE_INTERFACE);
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testTypeMatcherForClass() {
    Matcher matcher = TypeMatcher.builder().asClass().build();
    ClassVisitor visitor = parse(SIMPLE_CLASS);
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testExactClassNameMatch() {
    // Given
    String classWithName = "package x;" +
            "public class TestName";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withName("x.TestName")
            .build();

    // When
    ClassVisitor visitor = parse(classWithName);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testExactClassNameDoesNotMatch() {
    // Given
    String classWithName = "package x;" +
            "public class TestName";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withName("TestName2")
            .build();

    // When
    ClassVisitor visitor = parse(classWithName);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testRegularExpressionClassNameMatch() {
    // Given
    String classWithName = "package x;" +
            "public class TestName";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withNameLike("x.{3,5}Name")
            .build();

    // When
    ClassVisitor visitor = parse(classWithName);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testRegularExpressionClassNameWithNoMatch() {
    // Given
    String classWithName = "package x;" +
            "public class TestingName";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withNameLike(".{3,5}Name")
            .build();

    // When
    ClassVisitor visitor = parse(classWithName);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testTypeMatcherForClassImplementingInterface() {
    Matcher matcher = TypeMatcher.builder().asClass().implementingInterfaces(new HashSet<>(Arrays.asList("org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface"))).build();
    ClassVisitor visitor = parse(CLASS_IMPLEMENTS_INTERFACE);
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testTypeMatcherForClassImplementingQualifiedInterface() {
    Matcher matcher = TypeMatcher.builder().asClass().implementingInterfaces(new HashSet<>(Arrays.asList("org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface"))).build();
    ClassVisitor visitor = parse(CLASS_IMPLEMENTS_QUALIFIED_INTERFACE);
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testTypeMatcherForClassImplementingInterfaceAndExtendsClass() {
    Matcher matcher = TypeMatcher.builder().asClass().extending(B.class.getName()).build();
    ClassVisitor visitor = parse(CLASS_IMPLEMENTS_INTERFACE_AND_EXTENDS_CLASS);
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testTypeMatcherForClassImplementingInterfaceAndExtendsClassCheckingBoth() {
    Matcher matcher = TypeMatcher.builder().asClass()
        .extending(B.class.getName())
        .implementingInterfaces(new HashSet<>(Arrays.asList("org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface")))
        .build();
    ClassVisitor visitor = parse(CLASS_IMPLEMENTS_INTERFACE_AND_EXTENDS_CLASS);
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }

  
  @Test
  public void testClassExtendingSingleFullyQualifiedInterface() {
    // Given
    String fullyQualifiedInterface = "package x;" +
            "class Test implements org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface {}";
    Matcher matcher = TypeMatcher.builder().asClass().implementingInterfaces(new HashSet<>(Arrays.asList("org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface"))).build();

    // When
    ClassVisitor visitor = parse(fullyQualifiedInterface);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }


  @Test
  public void testClassExtendingTwoFullyQualifiedInterfaces() {
    // Given
    String fullyQualifiedInterface = "package x;" +
            "class Test implements org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface, java.util.List {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .implementingInterfaces(new HashSet<>(Arrays.asList("org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface", "java.util.List")))
            .build();

    // When
    ClassVisitor visitor = parse(fullyQualifiedInterface);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }

  
  @Test
  public void testClassExtendingThreeFullyQualifiedInterfaces() {
    // Given
    String fullyQualifiedInterface = "package x;" +
            "import java.util.Formattable;" +
            "class Test implements org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface, java.util.List, Formattable {}";

    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .implementingInterfaces(new HashSet<>(Arrays.asList("java.util.Formattable", "org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface", "java.util.List")))
            .build();

    // When
    ClassVisitor visitor = parse(fullyQualifiedInterface);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassWithSingleSimpleAnnotation() {
    // Given
    String annotatedClass = "package x;" +
            "import org.alfasoftware.astra.exampleTypes.AnnotationA;" +
            "@AnnotationA " +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withAnnotation()
            .withFullyQualifiedName("org.alfasoftware.astra.exampleTypes.AnnotationA").endTypeAnnotation()
            .build();

    // When
    ClassVisitor visitor = parse(annotatedClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassWithThreeAnnotations() {
    // Given
    String annotatedClass = "package x;\r\n" +
            "@Annotation\r\n" +
            "@Annotation2(3)\r\n" +
            "@Annotation3(marker=\"test\")\r\n" +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withAnnotation().withFullyQualifiedName("Annotation").endTypeAnnotation()
            .withAnnotation().withFullyQualifiedName("Annotation2").withValue(3).endTypeAnnotation()
            .withAnnotation().withFullyQualifiedName("Annotation3").withWithMemberAndValue("marker", "test").endTypeAnnotation()
            .build();

    // When
    ClassVisitor visitor = parse(annotatedClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassWithSingleAnnotation() {
    // Given
    String annotatedClass = "package x;\r\n" +
            "import org.alfa.Annotation;\r\n" +
            "@Annotation(marker=3)\r\n" +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withAnnotation()
            .withFullyQualifiedName("Annotation").withWithMemberAndValue("marker", 3).endTypeAnnotation()
            .build();

    // When
    ClassVisitor visitor = parse(annotatedClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassWithMissingSingleAnnotation() {
    // Given
    String annotatedClass = "package x;\r\n" +
            "import org.alfa.Annotation;\r\n" +
            "@Annotation(marker=3)\r\n" +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withAnnotation()
            .withFullyQualifiedName("Annotation2").withWithMemberAndValue("marker", 3).endTypeAnnotation()
            .build();

    // When
    ClassVisitor visitor = parse(annotatedClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassVisibilityIsPublic() {
    // Given
    String publicClass = "package x;" +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withPublicVisibility()
            .build();

    // When
    ClassVisitor visitor = parse(publicClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassVisibilityIsPublicWhenItIsNot() {
    // Given
    String publicClass = "package x;" +
            "class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withPublicVisibility()
            .build();

    // When
    ClassVisitor visitor = parse(publicClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassVisibilityIsPrivate() {
    // Given
    String publicClass = "package x;" +
            "private class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withPrivateVisibility()
            .build();

    // When
    ClassVisitor visitor = parse(publicClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassVisibilityIsPrivateWhenItIsNot() {
    // Given
    String publicClass = "package x;" +
            "class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withPrivateVisibility()
            .build();

    // When
    ClassVisitor visitor = parse(publicClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassVisibilityIsPackage() {
    // Given
    String publicClass = "package x;" +
            "class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withPackageVisibility()
            .build();

    // When
    ClassVisitor visitor = parse(publicClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassVisibilityIsPackageWhenItIsNot() {
    // Given
    String publicClass = "package x;" +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withPackageVisibility()
            .build();

    // When
    ClassVisitor visitor = parse(publicClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassIsStatic() {
    // Given
    String staticClass = "package x;" +
            "public static class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .isStatic()
            .build();

    // When
    ClassVisitor visitor = parse(staticClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassIsStaticWhenItIsNot() {
    // Given
    String staticClass = "package x;" +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .isStatic()
            .build();

    // When
    ClassVisitor visitor = parse(staticClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassIsAbstract() {
    // Given
    String staticClass = "package x;" +
            "public abstract class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .isAbstract()
            .build();

    // When
    ClassVisitor visitor = parse(staticClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassIsAbstractWhenItIsNot() {
    // Given
    String staticClass = "package x;" +
            "public class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .isAbstract()
            .build();

    // When
    ClassVisitor visitor = parse(staticClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertFalse(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testClassIsFinal() {
    // Given
    String staticClass = "package x;" +
            "public final class y {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .isFinal()
            .build();

    // When
    ClassVisitor visitor = parse(staticClass);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }
  

  @Test
  public void testComplexMatchOnManyCriteria() {
    // Given
    String complexTest = "package x;\r\n" +
            "import java.util.List;\r\n" +
            "import org.alfasoftware.astra.exampleTypes.AnnotationA;\r\n" +
            "\r\n" +
            "@AnnotationA(test=\"a\")\r\n" +
            "public abstract static class SimpleTest3141 extends org.alfasoftware.astra.exampleTypes.B implements List, org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface {}";
    Matcher matcher = TypeMatcher.builder()
            .asClass()
            .withNameLike("x.SimpleTest\\d{4}")
            .isAbstract()
            .isStatic()
            .extending("org.alfasoftware.astra.exampleTypes.B")
            .implementingInterfaces(new HashSet<>(Arrays.asList("java.util.List", "org.alfasoftware.astra.exampleTypes.ExampleMarkerInterface")))
            .withAnnotation().withFullyQualifiedName("org.alfasoftware.astra.exampleTypes.AnnotationA").withWithMemberAndValue("test", "a").endTypeAnnotation()
            .build();

    // When
    ClassVisitor visitor = parse(complexTest);

    // Then
    List<TypeDeclaration> typeDeclarations = visitor.getTypeDeclarations();
    assertTrue(matcher.matches(typeDeclarations.get(0)));
  }


  private ClassVisitor parse(String source) {
    CompilationUnit compilationUnit = AstraUtils.readAsCompilationUnit(source, new String[] {TEST_SOURCE}, UseCase.defaultClasspathEntries.toArray(new String[0]));
    ClassVisitor visitor = new ClassVisitor();
    compilationUnit.accept(visitor);
    return visitor;
  }
}
