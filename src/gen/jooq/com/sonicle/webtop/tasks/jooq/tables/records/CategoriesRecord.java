/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.tasks.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CategoriesRecord extends org.jooq.impl.UpdatableRecordImpl<com.sonicle.webtop.tasks.jooq.tables.records.CategoriesRecord> implements org.jooq.Record10<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean> {

	private static final long serialVersionUID = 1855366772;

	/**
	 * Setter for <code>tasks.categories.category_id</code>.
	 */
	public void setCategoryId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>tasks.categories.category_id</code>.
	 */
	public java.lang.Integer getCategoryId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>tasks.categories.domain_id</code>.
	 */
	public void setDomainId(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>tasks.categories.domain_id</code>.
	 */
	public java.lang.String getDomainId() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>tasks.categories.user_id</code>.
	 */
	public void setUserId(java.lang.String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>tasks.categories.user_id</code>.
	 */
	public java.lang.String getUserId() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>tasks.categories.built_in</code>.
	 */
	public void setBuiltIn(java.lang.Boolean value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>tasks.categories.built_in</code>.
	 */
	public java.lang.Boolean getBuiltIn() {
		return (java.lang.Boolean) getValue(3);
	}

	/**
	 * Setter for <code>tasks.categories.name</code>.
	 */
	public void setName(java.lang.String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>tasks.categories.name</code>.
	 */
	public java.lang.String getName() {
		return (java.lang.String) getValue(4);
	}

	/**
	 * Setter for <code>tasks.categories.description</code>.
	 */
	public void setDescription(java.lang.String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>tasks.categories.description</code>.
	 */
	public java.lang.String getDescription() {
		return (java.lang.String) getValue(5);
	}

	/**
	 * Setter for <code>tasks.categories.color</code>.
	 */
	public void setColor(java.lang.String value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>tasks.categories.color</code>.
	 */
	public java.lang.String getColor() {
		return (java.lang.String) getValue(6);
	}

	/**
	 * Setter for <code>tasks.categories.sync</code>.
	 */
	public void setSync(java.lang.String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>tasks.categories.sync</code>.
	 */
	public java.lang.String getSync() {
		return (java.lang.String) getValue(7);
	}

	/**
	 * Setter for <code>tasks.categories.is_private</code>.
	 */
	public void setIsPrivate(java.lang.Boolean value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>tasks.categories.is_private</code>.
	 */
	public java.lang.Boolean getIsPrivate() {
		return (java.lang.Boolean) getValue(8);
	}

	/**
	 * Setter for <code>tasks.categories.is_default</code>.
	 */
	public void setIsDefault(java.lang.Boolean value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>tasks.categories.is_default</code>.
	 */
	public java.lang.Boolean getIsDefault() {
		return (java.lang.Boolean) getValue(9);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record10 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row10<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean> fieldsRow() {
		return (org.jooq.Row10) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row10<java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean> valuesRow() {
		return (org.jooq.Row10) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.CATEGORY_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.DOMAIN_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.USER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field4() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.BUILT_IN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field5() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field6() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field7() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.COLOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field8() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.SYNC;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field9() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.IS_PRIVATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Boolean> field10() {
		return com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.IS_DEFAULT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getCategoryId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getDomainId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value4() {
		return getBuiltIn();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value5() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value6() {
		return getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value7() {
		return getColor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value8() {
		return getSync();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value9() {
		return getIsPrivate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Boolean value10() {
		return getIsDefault();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value1(java.lang.Integer value) {
		setCategoryId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value2(java.lang.String value) {
		setDomainId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value3(java.lang.String value) {
		setUserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value4(java.lang.Boolean value) {
		setBuiltIn(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value5(java.lang.String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value6(java.lang.String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value7(java.lang.String value) {
		setColor(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value8(java.lang.String value) {
		setSync(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value9(java.lang.Boolean value) {
		setIsPrivate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord value10(java.lang.Boolean value) {
		setIsDefault(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CategoriesRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3, java.lang.Boolean value4, java.lang.String value5, java.lang.String value6, java.lang.String value7, java.lang.String value8, java.lang.Boolean value9, java.lang.Boolean value10) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached CategoriesRecord
	 */
	public CategoriesRecord() {
		super(com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES);
	}

	/**
	 * Create a detached, initialised CategoriesRecord
	 */
	public CategoriesRecord(java.lang.Integer categoryId, java.lang.String domainId, java.lang.String userId, java.lang.Boolean builtIn, java.lang.String name, java.lang.String description, java.lang.String color, java.lang.String sync, java.lang.Boolean isPrivate, java.lang.Boolean isDefault) {
		super(com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES);

		setValue(0, categoryId);
		setValue(1, domainId);
		setValue(2, userId);
		setValue(3, builtIn);
		setValue(4, name);
		setValue(5, description);
		setValue(6, color);
		setValue(7, sync);
		setValue(8, isPrivate);
		setValue(9, isDefault);
	}
}
