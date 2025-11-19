Ext.define('Sonicle.overrides.webtop.tasks.Service', {
	override: 'Sonicle.webtop.tasks.Service',
	
	privates: {
		createGridCfg: function(tagsStore, nest, cfg) {
			var me = this,
				cfg = me.callParent(arguments);
			cfg.cls = Sonicle.String.join(' ', cfg.cls, 'x-grid-rounded');
			return cfg;
		}
	}
});