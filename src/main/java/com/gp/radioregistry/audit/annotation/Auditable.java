package com.gp.radioregistry.audit.annotation;

import com.gp.radioregistry.enums.EntityType;
import com.gp.radioregistry.enums.EventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
	EventType eventType();
	EntityType entityType();
	String entityId() default "";
	String description() default "";
}
