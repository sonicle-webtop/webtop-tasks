/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.tasks.jooq;

/**
 * Convenience access to all sequences in tasks
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sequences {

	/**
	 * The sequence <code>tasks.seq_categories</code>
	 */
	public static final org.jooq.Sequence<java.lang.Long> SEQ_CATEGORIES = new org.jooq.impl.SequenceImpl<java.lang.Long>("seq_categories", com.sonicle.webtop.tasks.jooq.Tasks.TASKS, org.jooq.impl.SQLDataType.BIGINT.nullable(false));

	/**
	 * The sequence <code>tasks.seq_tasks</code>
	 */
	public static final org.jooq.Sequence<java.lang.Long> SEQ_TASKS = new org.jooq.impl.SequenceImpl<java.lang.Long>("seq_tasks", com.sonicle.webtop.tasks.jooq.Tasks.TASKS, org.jooq.impl.SQLDataType.BIGINT.nullable(false));
}
