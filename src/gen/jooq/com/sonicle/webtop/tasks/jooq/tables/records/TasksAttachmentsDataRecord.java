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
public class TasksAttachmentsDataRecord extends org.jooq.impl.UpdatableRecordImpl<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsDataRecord> implements org.jooq.Record2<java.lang.String, byte[]> {

	private static final long serialVersionUID = 292106486;

	/**
	 * Setter for <code>tasks.tasks_attachments_data.task_attachment_id</code>.
	 */
	public void setTaskAttachmentId(java.lang.String value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>tasks.tasks_attachments_data.task_attachment_id</code>.
	 */
	public java.lang.String getTaskAttachmentId() {
		return (java.lang.String) getValue(0);
	}

	/**
	 * Setter for <code>tasks.tasks_attachments_data.bytes</code>.
	 */
	public void setBytes(byte[] value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>tasks.tasks_attachments_data.bytes</code>.
	 */
	public byte[] getBytes() {
		return (byte[]) getValue(1);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.String> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record2 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.String, byte[]> fieldsRow() {
		return (org.jooq.Row2) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row2<java.lang.String, byte[]> valuesRow() {
		return (org.jooq.Row2) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field1() {
		return com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA.TASK_ATTACHMENT_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<byte[]> field2() {
		return com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA.BYTES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value1() {
		return getTaskAttachmentId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] value2() {
		return getBytes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TasksAttachmentsDataRecord value1(java.lang.String value) {
		setTaskAttachmentId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TasksAttachmentsDataRecord value2(byte[] value) {
		setBytes(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TasksAttachmentsDataRecord values(java.lang.String value1, byte[] value2) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached TasksAttachmentsDataRecord
	 */
	public TasksAttachmentsDataRecord() {
		super(com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA);
	}

	/**
	 * Create a detached, initialised TasksAttachmentsDataRecord
	 */
	public TasksAttachmentsDataRecord(java.lang.String taskAttachmentId, byte[] bytes) {
		super(com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA);

		setValue(0, taskAttachmentId);
		setValue(1, bytes);
	}
}
