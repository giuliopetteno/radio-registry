package com.gp.radioregistry.audit.annotation;

import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.audit.enums.AuditEntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
	AuditAction action();
	AuditEntityType entityType();
	String entityId() default "";
	String description() default "";
}
