/**
 * This class is generated by jOOQ
 */
package com.sonicle.webtop.tasks.jooq;

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
public class Tasks extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = -578379478;

	/**
	 * The reference instance of <code>tasks</code>
	 */
	public static final Tasks TASKS = new Tasks();

	/**
	 * No further instances allowed
	 */
	private Tasks() {
		super("tasks");
	}

	@Override
	public final java.util.List<org.jooq.Sequence<?>> getSequences() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getSequences0());
		return result;
	}

	private final java.util.List<org.jooq.Sequence<?>> getSequences0() {
		return java.util.Arrays.<org.jooq.Sequence<?>>asList(
			com.sonicle.webtop.tasks.jooq.Sequences.SEQ_CATEGORIES,
			com.sonicle.webtop.tasks.jooq.Sequences.SEQ_TASKS);
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			com.sonicle.webtop.tasks.jooq.tables.Categories.CATEGORIES,
			com.sonicle.webtop.tasks.jooq.tables.Tasks.TASKS);
	}
}
