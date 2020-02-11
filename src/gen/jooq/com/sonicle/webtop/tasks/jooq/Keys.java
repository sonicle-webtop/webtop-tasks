/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.tasks.jooq;

/**
 * A class modelling foreign key relationships between tables of the <code>tasks</code> 
 * schema
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.Identity<com.sonicle.webtop.tasks.jooq.tables.records.CategoriesRecord, java.lang.Integer> IDENTITY_CATEGORIES = Identities0.IDENTITY_CATEGORIES;

	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.CategoriesRecord> CATEGORIES_PKEY = UniqueKeys0.CATEGORIES_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.CategoryPropsRecord> CATEGORY_PROPS_PKEY = UniqueKeys0.CATEGORY_PROPS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord> TASKS_PKEY = UniqueKeys0.TASKS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsRecord> TASKS_ATTACHMENTS_PKEY = UniqueKeys0.TASKS_ATTACHMENTS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsDataRecord> TASKS_ATTACHMENTS_DATA_PKEY = UniqueKeys0.TASKS_ATTACHMENTS_DATA_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksIcalendarsRecord> TASKS_ICALENDARS_PKEY = UniqueKeys0.TASKS_ICALENDARS_PKEY;
	public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksTagsRecord> TASKS_TAGS_PKEY = UniqueKeys0.TASKS_TAGS_PKEY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------

	public static final org.jooq.ForeignKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsRecord, com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord> TASKS_ATTACHMENTS__TASKS_ATTACHMENTS_TASK_ID_FKEY = ForeignKeys0.TASKS_ATTACHMENTS__TASKS_ATTACHMENTS_TASK_ID_FKEY;
	public static final org.jooq.ForeignKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsDataRecord, com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsRecord> TASKS_ATTACHMENTS_DATA__TASKS_ATTACHMENTS_DATA_TASK_ATTACHMENT_ID_FKEY = ForeignKeys0.TASKS_ATTACHMENTS_DATA__TASKS_ATTACHMENTS_DATA_TASK_ATTACHMENT_ID_FKEY;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class Identities0 extends org.jooq.impl.AbstractKeys {
		public static org.jooq.Identity<com.sonicle.webtop.tasks.jooq.tables.records.CategoriesRecord, java.lang.Integer> IDENTITY_CATEGORIES = createIdentity(com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES, com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.CATEGORY_ID);
	}

	private static class UniqueKeys0 extends org.jooq.impl.AbstractKeys {
		public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.CategoriesRecord> CATEGORIES_PKEY = createUniqueKey(com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES, com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES.CATEGORY_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.CategoryPropsRecord> CATEGORY_PROPS_PKEY = createUniqueKey(com.sonicle.webtop.tasks.jooq.tables.CategoryProps.CATEGORY_PROPS, com.sonicle.webtop.tasks.jooq.tables.CategoryProps.CATEGORY_PROPS.DOMAIN_ID, com.sonicle.webtop.tasks.jooq.tables.CategoryProps.CATEGORY_PROPS.USER_ID, com.sonicle.webtop.tasks.jooq.tables.CategoryProps.CATEGORY_PROPS.CATEGORY_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord> TASKS_PKEY = createUniqueKey(com.sonicle.webtop.tasks.jooq.tables.Tasks.TASKS, com.sonicle.webtop.tasks.jooq.tables.Tasks.TASKS.TASK_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsRecord> TASKS_ATTACHMENTS_PKEY = createUniqueKey(com.sonicle.webtop.tasks.jooq.tables.TasksAttachments.TASKS_ATTACHMENTS, com.sonicle.webtop.tasks.jooq.tables.TasksAttachments.TASKS_ATTACHMENTS.TASK_ATTACHMENT_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsDataRecord> TASKS_ATTACHMENTS_DATA_PKEY = createUniqueKey(com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA, com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA.TASK_ATTACHMENT_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksIcalendarsRecord> TASKS_ICALENDARS_PKEY = createUniqueKey(com.sonicle.webtop.tasks.jooq.tables.TasksIcalendars.TASKS_ICALENDARS, com.sonicle.webtop.tasks.jooq.tables.TasksIcalendars.TASKS_ICALENDARS.TASK_ID);
		public static final org.jooq.UniqueKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksTagsRecord> TASKS_TAGS_PKEY = createUniqueKey(com.sonicle.webtop.tasks.jooq.tables.TasksTags.TASKS_TAGS, com.sonicle.webtop.tasks.jooq.tables.TasksTags.TASKS_TAGS.TASK_ID, com.sonicle.webtop.tasks.jooq.tables.TasksTags.TASKS_TAGS.TAG_ID);
	}

	private static class ForeignKeys0 extends org.jooq.impl.AbstractKeys {
		public static final org.jooq.ForeignKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsRecord, com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord> TASKS_ATTACHMENTS__TASKS_ATTACHMENTS_TASK_ID_FKEY = createForeignKey(com.sonicle.webtop.tasks.jooq.Keys.TASKS_PKEY, com.sonicle.webtop.tasks.jooq.tables.TasksAttachments.TASKS_ATTACHMENTS, com.sonicle.webtop.tasks.jooq.tables.TasksAttachments.TASKS_ATTACHMENTS.TASK_ID);
		public static final org.jooq.ForeignKey<com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsDataRecord, com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsRecord> TASKS_ATTACHMENTS_DATA__TASKS_ATTACHMENTS_DATA_TASK_ATTACHMENT_ID_FKEY = createForeignKey(com.sonicle.webtop.tasks.jooq.Keys.TASKS_ATTACHMENTS_PKEY, com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA, com.sonicle.webtop.tasks.jooq.tables.TasksAttachmentsData.TASKS_ATTACHMENTS_DATA.TASK_ATTACHMENT_ID);
	}
}
