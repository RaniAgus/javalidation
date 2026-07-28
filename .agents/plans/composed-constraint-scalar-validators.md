# Reusable Generated Validators for Composed Constraints

## Summary

Generate one public scalar validator per reusable Jakarta composed constraint annotated with `@Constraint(validatedBy = {})`. Generated record validators instantiate/configure that scalar validator directly; they do not use `ValidatorsHolder`. Support recursive composition, repeatable usages, `@OverridesAttribute`, and Jakarta `@ReportAsSingleViolation` semantics.

## Implementation Changes

- Discover composed constraints on record components, accessors, type uses, and repeatable containers when their annotation type declares `@Constraint(validatedBy = {})`.
- Generate a public scalar validator beside each annotation, named `<AnnotationName>Validator` (for example, `com.example.NameValidator`), implementing `Validator<T>` where `T` is the single type safely inferred from all composing built-in constraints.
  - Generate a clear compilation error if that type name already exists in the annotation package.
  - Warn and skip compositions whose constraints have no common safe scalar type.
  - Keep `ConstraintValidator`-backed annotations unsupported; warn and skip them.
- Give generated scalar validators constructors containing the effective runtime configuration needed by that usage:
  - the outer `message` for `@ReportAsSingleViolation`;
  - every composing-constraint attribute overridden through `@OverridesAttribute` / `@OverridesAttribute.List`, including indexed repeatable constraints.
- Generate a `static final` scalar-validator instance in each record validator for each distinct effective configuration, and invoke it inside the existing `validation.withField(...)` scope.
- Preserve reporting semantics:
  - normal composition emits the composing constraints’ errors;
  - `@ReportAsSingleViolation` emits one outer-message error when any composing validation fails;
  - recursive compositions retain the nearest applicable single-violation boundary and preserve existing null short-circuit behavior.
- Add resolved-annotation/context handling to read defaults and explicit values, apply overrides, preserve declaration order, allocate unique generated constant names, and detect composition cycles.
- Update processor docs, Jakarta feature docs, and known limitations to document generated scalar validators, their naming/collision rule, inferred-type restriction, and remaining unsupported validator-backed constraints.

## Test Plan

- Add the supplied `@Name` annotation fixture plus expected generated `NameValidator` and record validator sources.
- Verify runtime behavior for null, blank, invalid pattern, valid names, and one outer `Name.message` error under `@ReportAsSingleViolation`.
- Cover scalar-validator reuse from multiple records and fields, distinct constructor configurations, recursive composition, repeatable composed constraints, and direct constraints alongside composition.
- Cover `@OverridesAttribute` for message, value attributes, and indexed repeatable composing constraints.
- Verify diagnostics for generated-name collisions, validator-backed constraints, unsupported/no-common-type compositions, invalid overrides, and cycles.
- Run `mvn test -pl javalidation-jakarta-validator-processor`.

## Assumptions

- V1 supports only compositions whose built-in constraints infer one common scalar type; `@Name` infers `CharSequence`.
- Validation groups remain always-on with the existing warning behavior.
- Generated scalar validators are public generated API in the annotation’s package; the record-validator registry remains record-focused and unchanged.
