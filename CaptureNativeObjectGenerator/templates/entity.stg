propertyDeclaration(attr) ::= <<
/**
 * <attr.description>
 */
private <attr.javaType> <attr.propertyName>;


>>

entity(package, className, attrs) ::= <<
/**
 * WARNING this code is automatically generated. Any changes made here will be overwritten.
 */
package <package>;

public class <className> extends JRCaptureEntity {
    <attrs:propertyDeclaration()>
}

>>
