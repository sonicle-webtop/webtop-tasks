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
public class TasksTagsRecord extends org.jooq.impl.UpdatableRecordImpl<com.sonicle.webtop.tasks.jooq.tables.records.TasksTagsRecord> implements org.jooq.Record2<java.lang.Integer, java.lang.String> {

	private static final long serialVersionUID = -1270688352;

	/**
	 * Setter for <code>tasks.tasks_tags.task_id</code>.
	 */
	public void setTaskId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>tasks.tasks_tags.task_id</code>.
	 */
	public java.lang.Integer getTaskId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>tasks.tasks_tags.tag_id</code>.
	 */
	public void setTagId(java.lang.String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>tasks.tasks_tags.tag_id</code>.
	 */
	public java.lang.String getTagId() {
		return (java.lang.String) getValue(1);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record2<java.lang.Integer, java.lang.String> key() {
		return (org.jooq.Record2) super.key();
	}

	// -------------------------------------------------------------------------
	// Record2 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.String> fieldsRow() {
		return (org.jooq.Row2) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.Integer, java.lang.String> valuesRow() {
		return (org.jooq.Row2) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return com.sonicle.webtop.tasks.jooq.tables.TasksTags.TASKS_TAGS.TASK_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return com.sonicle.webtop.tasks.jooq.tables.TasksTags.TASKS_TAGS.TAG_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getTaskId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getTagId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TasksTagsRecord value1(java.lang.Integer value) {
		setTaskId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TasksTagsRecord value2(java.lang.String value) {
		setTagId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TasksTagsRecord values(java.lang.Integer value1, java.lang.String value2) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached TasksTagsRecord
	 */
	public TasksTagsRecord() {
		super(com.sonicle.webtop.tasks.jooq.tables.TasksTags.TASKS_TAGS);
	}

	/**
	 * Create a detached, initialised TasksTagsRecord
	 */
	public TasksTagsRecord(java.lang.Integer taskId, java.lang.String tagId) {
		super(com.sonicle.webtop.tasks.jooq.tables.TasksTags.TASKS_TAGS);

		setValue(0, taskId);
		setValue(1, tagId);
	}
}
